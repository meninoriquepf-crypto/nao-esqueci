package com.naoesqueci.app.presentation.ui.screen.trip_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naoesqueci.app.domain.model.TripType
import com.naoesqueci.app.domain.usecase.item.DeleteItemUseCase
import com.naoesqueci.app.domain.usecase.item.GetItemsWithStateUseCase
import com.naoesqueci.app.domain.usecase.item.ToggleItemCheckUseCase
import com.naoesqueci.app.domain.usecase.trip.DeleteTripCascadeUseCase
import com.naoesqueci.app.domain.usecase.trip.GetTripUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TripDetailViewModel(
    private val getTripUseCase: GetTripUseCase,
    private val getItemsWithStateUseCase: GetItemsWithStateUseCase,
    private val toggleItemCheckUseCase: ToggleItemCheckUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val deleteTripUseCase: DeleteTripCascadeUseCase,
    private val tripId: Long
) : ViewModel() {

    val trip = getTripUseCase(tripId)

    val departureItems = getItemsWithStateUseCase.byType(tripId, TripType.DEPARTURE)
    val returnItems = getItemsWithStateUseCase.byType(tripId, TripType.RETURN)

    data class UiState(
        val selectedType: TripType = TripType.DEPARTURE,
        val showDeleteConfirm: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun setSelectedType(type: TripType) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun toggleCheck(itemId: Long, type: TripType, isChecked: Boolean) {
        viewModelScope.launch {
            toggleItemCheckUseCase(tripId, itemId, type, isChecked)
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            deleteItemUseCase(itemId)
        }
    }

    fun confirmDeleteTrip() {
        _uiState.update { it.copy(showDeleteConfirm = true) }
    }

    fun cancelDeleteTrip() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
    }

    fun executeDeleteTrip(onSuccess: () -> Unit) {
        viewModelScope.launch {
            deleteTripUseCase(tripId)
            onSuccess()
        }
    }
}