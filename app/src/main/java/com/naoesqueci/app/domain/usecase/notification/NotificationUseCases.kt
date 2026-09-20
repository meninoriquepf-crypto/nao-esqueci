package com.naoesqueci.app.domain.usecase.notification

import com.naoesqueci.app.alarm.AlarmScheduler
import com.naoesqueci.app.domain.model.Trip

class ScheduleTripNotificationsUseCase(private val alarmScheduler: AlarmScheduler) {
    suspend operator fun invoke(trip: Trip) {
        alarmScheduler.scheduleTripAlarms(trip)
    }
}

class CancelTripNotificationsUseCase(private val alarmScheduler: AlarmScheduler) {
    suspend operator fun invoke(trip: Trip) {
        alarmScheduler.cancelTripAlarms(trip)
    }
}

class CheckAndNotifyPendingItemsUseCase(
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(tripId: Long) {
        // Called by AlarmReceiver - implemented in data layer
    }
}