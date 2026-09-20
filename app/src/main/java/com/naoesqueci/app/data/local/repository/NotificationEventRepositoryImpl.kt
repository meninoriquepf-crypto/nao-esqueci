package com.naoesqueci.app.data.local.repository

import com.naoesqueci.app.data.local.database.dao.NotificationEventDao
import com.naoesqueci.app.data.local.database.entity.NotificationEventEntity
import com.naoesqueci.app.domain.model.NotificationEventType
import com.naoesqueci.app.domain.model.TripType
import com.naoesqueci.app.domain.repository.NotificationEventRepository

class NotificationEventRepositoryImpl(private val notificationEventDao: NotificationEventDao) : NotificationEventRepository {

    override suspend fun recordNotificationSent(tripId: Long, tripType: TripType, eventType: NotificationEventType) {
        notificationEventDao.insert(
            NotificationEventEntity(
                tripId = tripId,
                tripType = tripType.name,
                eventType = eventType.name
            )
        )
    }

    override suspend fun wasNotificationSent(tripId: Long, tripType: TripType, eventType: NotificationEventType): Boolean {
        return notificationEventDao.countSent(tripId, tripType.name, eventType.name) > 0
    }

    override suspend fun clearNotificationsForTrip(tripId: Long) = notificationEventDao.deleteByTripId(tripId)
    override suspend fun clearNotification(tripId: Long, tripType: TripType, eventType: NotificationEventType) = notificationEventDao.deleteByTripTypeAndEvent(tripId, tripType.name, eventType.name)
}