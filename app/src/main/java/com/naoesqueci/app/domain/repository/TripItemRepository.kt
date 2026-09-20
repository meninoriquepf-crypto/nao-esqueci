package com.naoesqueci.app.domain.repository

import com.naoesqueci.app.domain.model.TripItem
import kotlinx.coroutines.flow.Flow

interface TripItemRepository {
    suspend fun addItem(item: TripItem): Long
    suspend fun updateItem(item: TripItem)
    suspend fun deleteItem(itemId: Long)
    suspend fun deleteItemsByTripId(tripId: Long)
    fun getItemsByTripId(tripId: Long): Flow<List<TripItem>>
    fun getItemById(itemId: Long): Flow<TripItem?>
    fun suggestItemNames(prefix: String): Flow<List<String>>
}