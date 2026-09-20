package com.naoesqueci.app.data.local.repository

import com.naoesqueci.app.data.local.database.dao.TripItemDao
import com.naoesqueci.app.data.local.database.entity.TripItemEntity
import com.naoesqueci.app.domain.model.ItemCategory
import com.naoesqueci.app.domain.model.TripItem
import com.naoesqueci.app.domain.repository.TripItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TripItemRepositoryImpl(private val tripItemDao: TripItemDao) : TripItemRepository {

    private fun TripItemEntity.toDomain() = TripItem(
        id = id,
        tripId = tripId,
        name = name,
        category = try { ItemCategory.valueOf(category) } catch (e: Exception) { ItemCategory.NORMAL },
        requiredForDeparture = requiredForDeparture,
        requiredForReturn = requiredForReturn,
        displayOrder = displayOrder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun TripItem.toEntity() = TripItemEntity(
        id = id ?: 0,
        tripId = tripId,
        name = name,
        category = category.name,
        requiredForDeparture = requiredForDeparture,
        requiredForReturn = requiredForReturn,
        displayOrder = displayOrder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    override suspend fun addItem(item: TripItem): Long = tripItemDao.insert(item.toEntity())
    override suspend fun updateItem(item: TripItem) = tripItemDao.update(item.toEntity())
    override suspend fun deleteItem(itemId: Long) = tripItemDao.deleteById(itemId)
    override suspend fun deleteItemsByTripId(tripId: Long) = tripItemDao.deleteByTripId(tripId)
    override fun getItemsByTripId(tripId: Long): Flow<List<TripItem>> = tripItemDao.getByTripId(tripId).map { list -> list.map { it.toDomain() } }
    override fun getItemById(itemId: Long): Flow<TripItem?> = tripItemDao.getById(itemId).map { it?.toDomain() }
    override fun suggestItemNames(prefix: String): Flow<List<String>> {
        val escaped = prefix
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
        return tripItemDao.suggestNames(escaped)
    }
}