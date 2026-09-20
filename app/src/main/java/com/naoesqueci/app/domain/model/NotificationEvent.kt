package com.naoesqueci.app.domain.model

data class NotificationEvent(
    val id: Long? = null,
    val tripId: Long,
    val tripType: TripType,
    val eventType: NotificationEventType,
    val sentAt: Long = System.currentTimeMillis()
)