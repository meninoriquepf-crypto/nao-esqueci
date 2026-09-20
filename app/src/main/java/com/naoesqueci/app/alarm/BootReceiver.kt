package com.naoesqueci.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.naoesqueci.app.data.local.database.AppDatabase
import com.naoesqueci.app.data.local.preferences.SettingsDataStore
import com.naoesqueci.app.data.local.repository.SettingsRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            intent.action != "android.intent.action.MY_PACKAGE_REPLACED"
        ) {
            return
        }

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val tripDao = db.tripDao()
                val settingsRepo = SettingsRepositoryImpl(SettingsDataStore(context))
                val alarmScheduler = AlarmScheduler(context, settingsRepo)

                val now = System.currentTimeMillis()
                val upcomingTripEntities = tripDao.getUpcoming(now).first()
                val upcomingTrips = upcomingTripEntities.map { entity ->
                    com.naoesqueci.app.domain.model.Trip(
                        id = entity.id,
                        name = entity.name,
                        departureDateTime = entity.departureDateTime,
                        returnDateTime = entity.returnDateTime,
                        createdAt = entity.createdAt,
                        updatedAt = entity.updatedAt,
                        isCompleted = entity.isCompleted
                    )
                }
                alarmScheduler.rescheduleAllAlarms(upcomingTrips)
            } finally {
                pendingResult.finish()
            }
        }
    }
}