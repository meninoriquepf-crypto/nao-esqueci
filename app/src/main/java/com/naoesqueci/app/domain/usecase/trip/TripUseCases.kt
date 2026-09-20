package com.naoesqueci.app.domain.usecase.trip

import com.naoesqueci.app.alarm.AlarmScheduler
import com.naoesqueci.app.domain.model.Trip
import com.naoesqueci.app.domain.repository.CheckStateRepository
import com.naoesqueci.app.domain.repository.NotificationEventRepository
import com.naoesqueci.app.domain.repository.TripItemRepository
import com.naoesqueci.app.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow

class CreateTripUseCase(private val repository: TripRepository) {
    suspend operator fun invoke(trip: Trip): Long = repository.createTrip(trip)
}

class DeleteTripCascadeUseCase(
    private val tripRepository: TripRepository,
    private val tripItemRepository: TripItemRepository,
    private val checkStateRepository: CheckStateRepository,
    private val notificationEventRepository: NotificationEventRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(tripId: Long) {
        alarmScheduler.cancelAlarmsForTripId(tripId)
        tripItemRepository.deleteItemsByTripId(tripId)
        checkStateRepository.deleteCheckStatesByTripId(tripId)
        notificationEventRepository.clearNotificationsForTrip(tripId)
        tripRepository.deleteTrip(tripId)
    }
}

class GetTripUseCase(private val repository: TripRepository) {
    operator fun invoke(tripId: Long): Flow<Trip?> = repository.getTripById(tripId)
}

class GetTripsUseCase(private val repository: TripRepository) {
    fun getAll(): Flow<List<Trip>> = repository.getAllTrips()
    fun getUpcoming(): Flow<List<Trip>> = repository.getUpcomingTrips()
    fun getPast(): Flow<List<Trip>> = repository.getPastTrips()
    fun getCurrent(): Flow<List<Trip>> = repository.getCurrentTrips()
}

class UpdateTripUseCase(private val repository: TripRepository) {
    suspend operator fun invoke(trip: Trip) = repository.updateTrip(trip)
}