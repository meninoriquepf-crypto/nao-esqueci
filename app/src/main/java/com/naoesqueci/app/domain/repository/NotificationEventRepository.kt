package com.naoesqueci.app.domain.repository

import com.naoesqueci.app.domain.model.NotificationEventType
import com.naoesqueci.app.domain.model.TripType

interface NotificationEventRepository {
    suspend fun recordNotificationSent(tripId: Long, tripType: TripType, eventType: NotificationEventType)
    suspend fun wasNotificationSent(tripId: Long, tripType: TripType, eventType: NotificationEventType): Boolean
    suspend fun clearNotificationsForTrip(tripId: Long)
    suspend fun clearNotification(tripId: Long, tripType: TripType, eventType: NotificationEventType)
}