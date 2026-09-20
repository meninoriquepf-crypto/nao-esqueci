package com.naoesqueci.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.naoesqueci.app.data.local.database.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: TripEntity): Long

    @Update
    suspend fun update(trip: TripEntity)

    @Delete
    suspend fun delete(trip: TripEntity)

    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteById(tripId: Long)

    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun getById(tripId: Long): Flow<TripEntity?>

    @Query("SELECT * FROM trips ORDER BY departureDateTime ASC")
    fun getAll(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE departureDateTime > :now ORDER BY departureDateTime ASC")
    fun getUpcoming(now: Long): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE returnDateTime < :now ORDER BY departureDateTime ASC")
    fun getPast(now: Long): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE departureDateTime <= :now AND returnDateTime >= :now ORDER BY departureDateTime ASC")
    fun getCurrent(now: Long): Flow<List<TripEntity>>
}