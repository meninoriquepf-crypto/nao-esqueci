package com.naoesqueci.app.data.local.repository

import com.naoesqueci.app.data.local.database.dao.TripDao
import com.naoesqueci.app.data.local.database.entity.TripEntity
import com.naoesqueci.app.domain.model.Trip
import com.naoesqueci.app.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TripRepositoryImpl(private val tripDao: TripDao) : TripRepository {

    private fun TripEntity.toDomain() = Trip(
        id = id,
        name = name,
        departureDateTime = departureDateTime,
        returnDateTime = returnDateTime,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isCompleted = isCompleted
    )

    private fun Trip.toEntity() = TripEntity(
        id = id ?: 0,
        name = name,
        departureDateTime = departureDateTime,
        returnDateTime = returnDateTime,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isCompleted = isCompleted
    )

    override suspend fun createTrip(trip: Trip): Long = tripDao.insert(trip.toEntity())
    override suspend fun updateTrip(trip: Trip) = tripDao.update(trip.toEntity())
    override suspend fun deleteTrip(tripId: Long) = tripDao.deleteById(tripId)
    override fun getTripById(tripId: Long): Flow<Trip?> = tripDao.getById(tripId).map { it?.toDomain() }
    override fun getAllTrips(): Flow<List<Trip>> = tripDao.getAll().map { list -> list.map { it.toDomain() } }
    override fun getUpcomingTrips(): Flow<List<Trip>> = tripDao.getUpcoming(System.currentTimeMillis()).map { list -> list.map { it.toDomain() } }
    override fun getPastTrips(): Flow<List<Trip>> = tripDao.getPast(System.currentTimeMillis()).map { list -> list.map { it.toDomain() } }
    override fun getCurrentTrips(): Flow<List<Trip>> = tripDao.getCurrent(System.currentTimeMillis()).map { list -> list.map { it.toDomain() } }
}