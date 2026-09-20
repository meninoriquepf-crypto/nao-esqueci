package com.naoesqueci.app.presentation.ui.screen.trip_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naoesqueci.app.domain.usecase.settings.GetThemeUseCase
import com.naoesqueci.app.domain.usecase.trip.DeleteTripCascadeUseCase
import com.naoesqueci.app.domain.usecase.trip.GetTripsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TripListViewModel(
    private val getTripsUseCase: GetTripsUseCase,
    private val deleteTripUseCase: DeleteTripCascadeUseCase,
    private val getThemeUseCase: GetThemeUseCase
) : ViewModel() {

    val trips = getTripsUseCase.getAll()

    val theme = getThemeUseCase()

    data class UiState(
        val showDeleteConfirm: Boolean = false,
        val tripToDeleteId: Long? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun confirmDeleteTrip(tripId: Long) {
        _uiState.update { it.copy(showDeleteConfirm = true, tripToDeleteId = tripId) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(showDeleteConfirm = false, tripToDeleteId = null) }
    }

    fun executeDeleteTrip() {
        uiState.value.tripToDeleteId?.let { tripId ->
            viewModelScope.launch {
                deleteTripUseCase(tripId)
            }
            cancelDelete()
        }
    }
}