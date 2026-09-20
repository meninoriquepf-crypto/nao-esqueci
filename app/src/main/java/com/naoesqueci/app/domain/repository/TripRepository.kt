package com.naoesqueci.app.domain.repository

import com.naoesqueci.app.domain.model.Trip
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    suspend fun createTrip(trip: Trip): Long
    suspend fun updateTrip(trip: Trip)
    suspend fun deleteTrip(tripId: Long)
    fun getTripById(tripId: Long): Flow<Trip?>
    fun getAllTrips(): Flow<List<Trip>>
    fun getUpcomingTrips(): Flow<List<Trip>>
    fun getPastTrips(): Flow<List<Trip>>
    fun getCurrentTrips(): Flow<List<Trip>>
}