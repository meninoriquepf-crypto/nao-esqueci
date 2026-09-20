package com.naoesqueci.app.testdoubles

import com.naoesqueci.app.domain.model.Trip
import com.naoesqueci.app.domain.model.TripItem
import com.naoesqueci.app.domain.repository.SettingsRepository
import com.naoesqueci.app.domain.repository.TripItemRepository
import com.naoesqueci.app.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeTripRepository : TripRepository {
    private val trips = MutableStateFlow<List<Trip>>(emptyList())
    private var nextId = 1L

    override suspend fun createTrip(trip: Trip): Long {
        val id = nextId++
        trips.update { it + trip.copy(id = id) }
        return id
    }

    override suspend fun updateTrip(trip: Trip) {
        trips.update { list -> list.map { if (it.id == trip.id) trip else it } }
    }

    override suspend fun deleteTrip(tripId: Long) {
        trips.update { list -> list.filterNot { it.id == tripId } }
    }

    override fun getTripById(tripId: Long): Flow<Trip?> = trips.map { list -> list.find { it.id == tripId } }
    override fun getAllTrips(): Flow<List<Trip>> = trips
    override fun getUpcomingTrips(): Flow<List<Trip>> = trips.map { list -> list.filter { it.isDepartureInFuture() } }
    override fun getPastTrips(): Flow<List<Trip>> = trips.map { list -> list.filter { !it.isReturnInFuture() } }
    override fun getCurrentTrips(): Flow<List<Trip>> = trips
}

class FakeTripItemRepository : TripItemRepository {
    private val items = MutableStateFlow<List<TripItem>>(emptyList())
    private var nextId = 1L

    override suspend fun addItem(item: TripItem): Long {
        val id = nextId++
        items.update { it + item.copy(id = id) }
        return id
    }

    override suspend fun updateItem(item: TripItem) {
        items.update { list -> list.map { if (it.id == item.id) item else it } }
    }

    override suspend fun deleteItem(itemId: Long) {
        items.update { list -> list.filterNot { it.id == itemId } }
    }

    override suspend fun deleteItemsByTripId(tripId: Long) {
        items.update { list -> list.filterNot { it.tripId == tripId } }
    }

    override fun getItemsByTripId(tripId: Long): Flow<List<TripItem>> =
        items.map { list -> list.filter { it.tripId == tripId } }

    override fun getItemById(itemId: Long): Flow<TripItem?> =
        items.map { list -> list.find { it.id == itemId } }

    override fun suggestItemNames(prefix: String): Flow<List<String>> =
        items.map { list ->
            list.map { it.name }.distinct()
                .filter { it.contains(prefix, ignoreCase = true) }
                .sorted().take(5)
        }
}

class FakeSettingsRepository(
    notificationsEnabledValue: Boolean = true,
    advanceMinutesValue: Int = 30,
    repeatMinutesValue: Int = 10,
    themeValue: String = "system"
) : SettingsRepository {
    private val _notificationsEnabled = MutableStateFlow(notificationsEnabledValue)
    private val _advanceMinutes = MutableStateFlow(advanceMinutesValue)
    private val _repeatMinutes = MutableStateFlow(repeatMinutesValue)
    private val _theme = MutableStateFlow(themeValue)

    override val notificationsEnabled: Flow<Boolean> = _notificationsEnabled
    override val advanceMinutes: Flow<Int> = _advanceMinutes
    override val repeatMinutes: Flow<Int> = _repeatMinutes
    override val theme: Flow<String> = _theme

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    override suspend fun setAdvanceMinutes(minutes: Int) {
        _advanceMinutes.value = minutes
    }

    override suspend fun setRepeatMinutes(minutes: Int) {
        _repeatMinutes.value = minutes
    }

    override suspend fun setTheme(theme: String) {
        _theme.value = theme
    }
}
