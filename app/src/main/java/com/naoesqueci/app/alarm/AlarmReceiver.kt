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

    override fun onReceive(context: Context, intent: Intent) {
        val tripId = intent.getLongExtra(AlarmConstants.EXTRA_TRIP_ID, -1)
        val tripTypeStr = intent.getStringExtra(AlarmConstants.EXTRA_TRIP_TYPE) ?: return
        val eventType = intent.getStringExtra(AlarmConstants.EXTRA_EVENT_TYPE) ?: return

        if (tripId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                handleAlarm(context.applicationContext, tripId, tripTypeStr, eventType)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleAlarm(context: Context, tripId: Long, tripTypeStr: String, eventType: String) {
        if (!hasNotificationPermission(context)) return

        val db = AppDatabase.getDatabase(context)
        val settingsRepository = SettingsRepositoryImpl(SettingsDataStore(context))
        if (!settingsRepository.notificationsEnabled.first()) return

        val entity = db.tripDao().getById(tripId).first() ?: return
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

        if (pending.isNotEmpty()) {
            dispatcher.sendPendingItemsAlert(trip, tripType, pending.map { it.name })
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
