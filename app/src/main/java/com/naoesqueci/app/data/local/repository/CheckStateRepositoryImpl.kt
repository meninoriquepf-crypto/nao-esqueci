package com.naoesqueci.app.data.local.repository

import com.naoesqueci.app.data.local.database.dao.CheckStateDao
import com.naoesqueci.app.data.local.database.entity.CheckStateEntity
import com.naoesqueci.app.domain.model.CheckState
import com.naoesqueci.app.domain.model.TripType
import com.naoesqueci.app.domain.repository.CheckStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CheckStateRepositoryImpl(private val checkStateDao: CheckStateDao) : CheckStateRepository {

    private fun CheckStateEntity.toDomain() = CheckState(
        tripId = tripId,
        itemId = itemId,
        tripType = try { TripType.valueOf(tripType) } catch (e: Exception) { TripType.DEPARTURE },
        isChecked = isChecked,
        checkedAt = checkedAt
    )

    private fun CheckState.toEntity() = CheckStateEntity(
        tripId = tripId,
        itemId = itemId,
        tripType = tripType.name,
        isChecked = isChecked,
        checkedAt = checkedAt
    )

    override suspend fun upsertCheckState(checkState: CheckState) = checkStateDao.upsert(checkState.toEntity())
    override suspend fun upsertCheckStates(checkStates: List<CheckState>) = checkStateDao.upsertAll(checkStates.map { it.toEntity() })
    override fun getCheckStatesByTripAndType(tripId: Long, tripType: String): Flow<List<CheckState>> = checkStateDao.getByTripAndType(tripId, tripType).map { list -> list.map { it.toDomain() } }
    override fun getAllCheckStatesByTrip(tripId: Long): Flow<List<CheckState>> = checkStateDao.getAllByTrip(tripId).map { list -> list.map { it.toDomain() } }
    override suspend fun deleteCheckStatesByTripId(tripId: Long) = checkStateDao.deleteByTripId(tripId)
    override suspend fun deleteCheckStateByTripAndItem(tripId: Long, itemId: Long) = checkStateDao.deleteByTripAndItem(tripId, itemId)
    override fun getCheckState(tripId: Long, itemId: Long, tripType: String): Flow<CheckState?> = checkStateDao.getByTripItemAndType(tripId, itemId, tripType).map { it?.toDomain() }
}