package com.naoesqueci.app.alarm

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.naoesqueci.app.data.local.database.AppDatabase
import com.naoesqueci.app.data.local.database.entity.TripItemEntity
import com.naoesqueci.app.data.local.preferences.SettingsDataStore
import com.naoesqueci.app.data.local.repository.NotificationEventRepositoryImpl
import com.naoesqueci.app.data.local.repository.SettingsRepositoryImpl
import com.naoesqueci.app.data.notification.NotificationDispatcher
import com.naoesqueci.app.domain.model.ItemCategory
import com.naoesqueci.app.domain.model.NotificationEventType
import com.naoesqueci.app.domain.model.Trip
import com.naoesqueci.app.domain.model.TripType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NaoEsqueciAlarm"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val tripId = intent.getLongExtra(AlarmConstants.EXTRA_TRIP_ID, -1)
        val tripTypeStr = intent.getStringExtra(AlarmConstants.EXTRA_TRIP_TYPE) ?: return
        val eventType = intent.getStringExtra(AlarmConstants.EXTRA_EVENT_TYPE) ?: return
        val deferred = intent.getBooleanExtra(AlarmConstants.EXTRA_DEFERRED, false)

        if (tripId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (eventType == "SNOOZE" && deferred) {
                    handleSnoozeTap(context.applicationContext, tripId, tripTypeStr)
                } else {
                    handleAlarm(context.applicationContext, tripId, tripTypeStr, eventType)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleSnoozeTap(context: Context, tripId: Long, tripTypeStr: String) {
        val tripType = try { TripType.valueOf(tripTypeStr) } catch (e: Exception) { TripType.DEPARTURE }
        val settingsRepository = SettingsRepositoryImpl(SettingsDataStore(context))
        AlarmScheduler(context, settingsRepository).scheduleSnoozeAlarm(
            tripId,
            tripType,
            System.currentTimeMillis() + (AlarmConstants.SNOOZE_MINUTES * 60 * 1000)
        )
        val eventRepository = NotificationEventRepositoryImpl(AppDatabase.getDatabase(context).notificationEventDao())
        NotificationDispatcher(context, eventRepository, settingsRepository).dismissFinalAlert(tripId, tripType)
    }

    private suspend fun handleAlarm(context: Context, tripId: Long, tripTypeStr: String, eventType: String) {
        if (!hasNotificationPermission(context)) {
            android.util.Log.w(TAG, "alarme ignorado: sem permissao POST_NOTIFICATIONS (trip=$tripId)")
            return
        }

        val db = AppDatabase.getDatabase(context)
        val settingsRepository = SettingsRepositoryImpl(SettingsDataStore(context))
        if (!settingsRepository.notificationsEnabled.first()) {
            android.util.Log.i(TAG, "alarme ignorado: notificacoes desativadas (trip=$tripId)")
            return
        }

        val entity = db.tripDao().getById(tripId).first()
        if (entity == null) {
            android.util.Log.i(TAG, "alarme ignorado: viagem inexistente (trip=$tripId)")
            return
        }
        val trip = Trip(
            id = entity.id,
            name = entity.name,
            departureDateTime = entity.departureDateTime,
            returnDateTime = entity.returnDateTime,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            isCompleted = entity.isCompleted
        )
        val tripType = try { TripType.valueOf(tripTypeStr) } catch (e: Exception) { TripType.DEPARTURE }

        val items = db.tripItemDao().getByTripId(tripId).first().map { it.toDomainItem() }
        val required = items.filter { it.category == ItemCategory.NORMAL && it.requiredFor(tripType) }
        val checkedIds = db.checkStateDao().getByTripAndType(tripId, tripType.name).first()
            .filter { it.isChecked }
            .map { it.itemId }
            .toSet()
        val pending = required.filterNot { checkedIds.contains(it.id) }

        val eventRepository = NotificationEventRepositoryImpl(db.notificationEventDao())
        val dispatcher = NotificationDispatcher(context, eventRepository, settingsRepository)
        val scheduler = AlarmScheduler(context, settingsRepository)

        if (pending.isNotEmpty()) {
            android.util.Log.i(TAG, "alertando $eventType: ${pending.size} pendentes (trip=$tripId, type=$tripType)")
            if (eventType == "AT_TIME") {
                dispatcher.sendFinalAlert(trip, tripType, pending.map { it.name })
            } else {
                dispatcher.sendPendingItemsAlert(trip, tripType, pending.map { it.name })
            }
            if (eventType == "REPEAT_WARNING") {
                val repeatMinutes = settingsRepository.repeatMinutes.first()
                val tripTime = when (tripType) {
                    TripType.DEPARTURE -> trip.departureDateTime
                    TripType.RETURN -> trip.returnDateTime
                }
                val next = System.currentTimeMillis() + (repeatMinutes * 60 * 1000)
                if (repeatMinutes > 0 && next < tripTime) {
                    scheduler.scheduleRepeatAlarm(tripId, tripType, next)
                }
            }
            return
        }

        // Tudo conferido: confirma uma única vez no momento da viagem;
        // o aviso antecipado permanece silencioso para evitar alerta desnecessário.
        if (eventType == "AT_TIME" &&
            !eventRepository.wasNotificationSent(tripId, tripType, NotificationEventType.POSITIVE_CONFIRMATION)
        ) {
            dispatcher.sendPositiveConfirmation(trip, tripType)
            eventRepository.recordNotificationSent(tripId, tripType, NotificationEventType.POSITIVE_CONFIRMATION)
        }
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun TripItemEntity.toDomainItem() = com.naoesqueci.app.domain.model.TripItem(
        id = id,
        tripId = tripId,
        name = name,
        category = try { ItemCategory.valueOf(category) } catch (e: Exception) { ItemCategory.NORMAL },
        requiredForDeparture = requiredForDeparture,
        requiredForReturn = requiredForReturn,
        displayOrder = displayOrder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
