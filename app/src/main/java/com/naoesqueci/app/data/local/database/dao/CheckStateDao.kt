package com.naoesqueci.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.naoesqueci.app.data.local.database.entity.CheckStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(checkState: CheckStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(checkStates: List<CheckStateEntity>)

    @Query("SELECT * FROM check_states WHERE tripId = :tripId AND tripType = :tripType")
    fun getByTripAndType(tripId: Long, tripType: String): Flow<List<CheckStateEntity>>

    @Query("SELECT * FROM check_states WHERE tripId = :tripId")
    fun getAllByTrip(tripId: Long): Flow<List<CheckStateEntity>>

    @Query("DELETE FROM check_states WHERE tripId = :tripId")
    suspend fun deleteByTripId(tripId: Long)

    @Query("DELETE FROM check_states WHERE tripId = :tripId AND itemId = :itemId")
    suspend fun deleteByTripAndItem(tripId: Long, itemId: Long)

    @Query("SELECT * FROM check_states WHERE tripId = :tripId AND itemId = :itemId AND tripType = :tripType LIMIT 1")
    fun getByTripItemAndType(tripId: Long, itemId: Long, tripType: String): Flow<CheckStateEntity?>
}