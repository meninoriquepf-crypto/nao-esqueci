package com.naoesqueci.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.naoesqueci.app.data.local.database.entity.TripItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TripItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TripItemEntity>)

    @Update
    suspend fun update(item: TripItemEntity)

    @Delete
    suspend fun delete(item: TripItemEntity)

    @Query("DELETE FROM trip_items WHERE id = :itemId")
    suspend fun deleteById(itemId: Long)

    @Query("DELETE FROM trip_items WHERE tripId = :tripId")
    suspend fun deleteByTripId(tripId: Long)

    @Query("SELECT * FROM trip_items WHERE tripId = :tripId ORDER BY displayOrder ASC")
    fun getByTripId(tripId: Long): Flow<List<TripItemEntity>>

    @Query("SELECT * FROM trip_items WHERE id = :itemId")
    fun getById(itemId: Long): Flow<TripItemEntity?>

    @Query("SELECT DISTINCT name FROM trip_items WHERE name LIKE '%' || :escaped || '%' ESCAPE '\\' ORDER BY name ASC LIMIT 5")
    fun suggestNames(escaped: String): Flow<List<String>>
}