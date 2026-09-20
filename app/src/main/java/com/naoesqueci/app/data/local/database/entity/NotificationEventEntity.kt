package com.naoesqueci.app.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_events")
data class NotificationEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val tripType: String,
    val eventType: String,
    val sentAt: Long = System.currentTimeMillis()
)