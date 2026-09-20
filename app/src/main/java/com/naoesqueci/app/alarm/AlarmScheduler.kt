package com.naoesqueci.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.naoesqueci.app.domain.model.Trip
import com.naoesqueci.app.domain.model.TripType
import com.naoesqueci.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class AlarmScheduler(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun scheduleTripAlarms(trip: Trip) {
        val notificationsEnabled = settingsRepository.notificationsEnabled.first()
        if (!notificationsEnabled) return

        val advanceMinutes = settingsRepository.advanceMinutes.first()

        scheduleForType(trip, TripType.DEPARTURE, advanceMinutes)
        scheduleForType(trip, TripType.RETURN, advanceMinutes)
    }

    private fun scheduleForType(trip: Trip, type: TripType, advanceMinutes: Int) {
        val dateTime = when (type) {
            TripType.DEPARTURE -> trip.departureDateTime
            TripType.RETURN -> trip.returnDateTime
        }

        if (dateTime <= System.currentTimeMillis()) return

        if (advanceMinutes > 0) {
            val advanceTime = dateTime - (advanceMinutes * 60 * 1000)
            if (advanceTime > System.currentTimeMillis()) {
                scheduleAlarm(
                    tripId = trip.id ?: return,
                    tripType = type,
                    eventType = "ADVANCE_WARNING",
                    triggerTime = advanceTime
                )
            }
        }

        scheduleAlarm(
            tripId = trip.id ?: return,
            tripType = type,
            eventType = "AT_TIME",
            triggerTime = dateTime
        )
    }

    private fun scheduleAlarm(tripId: Long, tripType: TripType, eventType: String, triggerTime: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmConstants.generateAction(tripId, tripType.name, eventType)
            putExtra(AlarmConstants.EXTRA_TRIP_ID, tripId)
            putExtra(AlarmConstants.EXTRA_TRIP_TYPE, tripType.name)
            putExtra(AlarmConstants.EXTRA_EVENT_TYPE, eventType)
        }

        val requestCode = AlarmConstants.generateRequestCode(tripId, tripType.ordinal, getEventTypeOrdinal(eventType))
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerTime, pendingIntent), pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerTime, pendingIntent), pendingIntent)
        }
    }

    suspend fun cancelTripAlarms(trip: Trip) {
        trip.id?.let { cancelAlarmsForTripId(it) }
    }

    fun cancelAlarmsForTripId(tripId: Long) {
        cancelAlarm(tripId, TripType.DEPARTURE, "ADVANCE_WARNING")
        cancelAlarm(tripId, TripType.DEPARTURE, "AT_TIME")
        cancelAlarm(tripId, TripType.RETURN, "ADVANCE_WARNING")
        cancelAlarm(tripId, TripType.RETURN, "AT_TIME")
    }

    private fun cancelAlarm(tripId: Long, tripType: TripType, eventType: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmConstants.generateAction(tripId, tripType.name, eventType)
        }
        val requestCode = AlarmConstants.generateRequestCode(tripId, tripType.ordinal, getEventTypeOrdinal(eventType))
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun getEventTypeOrdinal(eventType: String): Int = when (eventType) {
        "ADVANCE_WARNING" -> 0
        "AT_TIME" -> 1
        else -> 0
    }

    suspend fun rescheduleAllAlarms(trips: List<Trip>) {
        trips.forEach { trip ->
            if (trip.isDepartureInFuture() || trip.isReturnInFuture()) {
                scheduleTripAlarms(trip)
            }
        }
    }
}