package com.naoesqueci.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.naoesqueci.app.data.local.database.entity.NotificationEventEntity

@Dao
interface NotificationEventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: NotificationEventEntity)

    @Query("SELECT COUNT(*) FROM notification_events WHERE tripId = :tripId AND tripType = :tripType AND eventType = :eventType")
    suspend fun countSent(tripId: Long, tripType: String, eventType: String): Int

    @Query("DELETE FROM notification_events WHERE tripId = :tripId")
    suspend fun deleteByTripId(tripId: Long)

    @Query("DELETE FROM notification_events WHERE tripId = :tripId AND tripType = :tripType AND eventType = :eventType")
    suspend fun deleteByTripTypeAndEvent(tripId: Long, tripType: String, eventType: String)
}