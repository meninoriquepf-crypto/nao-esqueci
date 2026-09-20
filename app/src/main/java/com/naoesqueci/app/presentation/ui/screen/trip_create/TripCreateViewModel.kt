package com.naoesqueci.app.presentation.ui.screen.trip_create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naoesqueci.app.domain.model.Trip
import com.naoesqueci.app.domain.util.DateTimeUtils
import com.naoesqueci.app.domain.usecase.notification.CancelTripNotificationsUseCase
import com.naoesqueci.app.domain.usecase.notification.ScheduleTripNotificationsUseCase
import com.naoesqueci.app.domain.usecase.trip.CreateTripUseCase
import com.naoesqueci.app.domain.usecase.trip.GetTripUseCase
import com.naoesqueci.app.domain.usecase.trip.UpdateTripUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TripCreateViewModel(
    private val createTripUseCase: CreateTripUseCase,
    private val updateTripUseCase: UpdateTripUseCase,
    private val getTripUseCase: GetTripUseCase,
    private val scheduleNotificationsUseCase: ScheduleTripNotificationsUseCase,
    private val cancelNotificationsUseCase: CancelTripNotificationsUseCase,
    private val tripId: Long?
) : ViewModel() {

    data class UiState(
        val name: String = "",
        val departureDate: Long = System.currentTimeMillis(),
        val departureTime: Long = System.currentTimeMillis(),
        val returnDate: Long = System.currentTimeMillis() + 86400000,
        val returnTime: Long = System.currentTimeMillis() + 86400000 + 3600000,
        val error: String? = null,
        val isLoading: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        if (tripId != null) {
            loadTrip()
        }
    }

    private fun loadTrip() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getTripUseCase(tripId!!).first()?.let { trip ->
                _uiState.update {
                    it.copy(
                        name = trip.name,
                        departureDate = trip.departureDateTime,
                        departureTime = trip.departureDateTime,
                        returnDate = trip.returnDateTime,
                        returnTime = trip.returnDateTime,
                        isLoading = false
                    )
                }
            } ?: run {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setName(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun setDepartureDateTime(dateTime: Long) {
        _uiState.update { it.copy(departureDate = dateTime, departureTime = dateTime, error = null) }
    }

    fun setDepartureDate(dateMillis: Long) {
        val combined = DateTimeUtils.combineDateTime(dateMillis, _uiState.value.departureTime)
        _uiState.update { it.copy(departureDate = combined, departureTime = combined, error = null) }
    }

    fun setDepartureTime(hour: Int, minute: Int) {
        val timeCal = java.util.Calendar.getInstance().apply {
            timeInMillis = _uiState.value.departureTime
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val combined = DateTimeUtils.combineDateTime(_uiState.value.departureDate, timeCal.timeInMillis)
        _uiState.update { it.copy(departureDate = combined, departureTime = combined, error = null) }
    }

    fun setReturnDateTime(dateTime: Long) {
        _uiState.update { it.copy(returnDate = dateTime, returnTime = dateTime, error = null) }
    }

    fun setReturnDate(dateMillis: Long) {
        val combined = DateTimeUtils.combineDateTime(dateMillis, _uiState.value.returnTime)
        _uiState.update { it.copy(returnDate = combined, returnTime = combined, error = null) }
    }

    fun setReturnTime(hour: Int, minute: Int) {
        val timeCal = java.util.Calendar.getInstance().apply {
            timeInMillis = _uiState.value.returnTime
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val combined = DateTimeUtils.combineDateTime(_uiState.value.returnDate, timeCal.timeInMillis)
        _uiState.update { it.copy(returnDate = combined, returnTime = combined, error = null) }
    }

    fun saveTrip(onSuccess: () -> Unit) {
        val state = _uiState.value
        val departure = DateTimeUtils.combineDateTime(state.departureDate, state.departureTime)
        val returnDateTime = DateTimeUtils.combineDateTime(state.returnDate, state.returnTime)

        if (state.name.trim().isEmpty()) {
            _uiState.update { it.copy(error = "Digite um nome para a viagem") }
            return
        }

        if (returnDateTime <= departure) {
            _uiState.update { it.copy(error = "A volta deve ser depois da ida") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val trip = Trip(
                id = tripId,
                name = state.name.trim(),
                departureDateTime = departure,
                returnDateTime = returnDateTime
            )

            if (tripId == null) {
                val newId = createTripUseCase(trip)
                scheduleNotificationsUseCase(trip.copy(id = newId))
            } else {
                updateTripUseCase(trip)
                cancelNotificationsUseCase(trip)
                scheduleNotificationsUseCase(trip)
            }

            _uiState.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }
}