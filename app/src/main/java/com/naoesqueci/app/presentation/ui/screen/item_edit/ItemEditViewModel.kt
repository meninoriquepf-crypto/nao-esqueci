package com.naoesqueci.app.presentation.ui.screen.item_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naoesqueci.app.domain.model.ItemCategory
import com.naoesqueci.app.domain.model.TripItem
import com.naoesqueci.app.domain.usecase.item.AddItemUseCase
import com.naoesqueci.app.domain.usecase.item.DeleteItemUseCase
import com.naoesqueci.app.domain.usecase.item.GetItemNameSuggestionsUseCase
import com.naoesqueci.app.domain.usecase.item.GetItemsUseCase
import com.naoesqueci.app.domain.usecase.item.UpdateItemUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ItemEditViewModel(
    private val addItemUseCase: AddItemUseCase,
    private val updateItemUseCase: UpdateItemUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val getItemUseCase: suspend (Long) -> TripItem?,
    private val getItemsUseCase: GetItemsUseCase,
    private val suggestNamesUseCase: GetItemNameSuggestionsUseCase,
    val tripId: Long,
    val itemId: Long?
) : ViewModel() {

    data class UiState(
        val name: String = "",
        val category: ItemCategory = ItemCategory.NORMAL,
        val requiredForDeparture: Boolean = true,
        val requiredForReturn: Boolean = true,
        val error: String? = null,
        val isLoading: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    @OptIn(ExperimentalCoroutinesApi::class)
    val suggestions: StateFlow<List<String>> = uiState
        .map { it.name.trim() }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.length < 2) flowOf(emptyList())
            else suggestNamesUseCase(query).map { names ->
                names.filter { !it.equals(query, ignoreCase = true) }.take(5)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        if (itemId != null) {
            loadItem()
        }
    }

    private fun loadItem() {
        viewModelScope.launch {
            getItemUseCase(itemId!!)?.let { item ->
                _uiState.update {
                    it.copy(
                        name = item.name,
                        category = item.category,
                        requiredForDeparture = item.requiredForDeparture,
                        requiredForReturn = item.requiredForReturn
                    )
                }
            }
        }
    }

    fun setName(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun setCategory(category: ItemCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun setRequiredForDeparture(required: Boolean) {
        _uiState.update { it.copy(requiredForDeparture = required) }
    }

    fun setRequiredForReturn(required: Boolean) {
        _uiState.update { it.copy(requiredForReturn = required) }
    }

    fun saveItem(onSuccess: () -> Unit) {
        val state = _uiState.value
        val displayName = state.name.trim().replaceFirstChar { it.uppercase() }

        if (displayName.isEmpty()) {
            _uiState.update { it.copy(error = "Digite um nome para o item") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val normalized = normalizeName(displayName)
            val duplicate = getItemsUseCase(tripId).first()
                .any { it.id != itemId && normalizeName(it.name) == normalized }
            if (duplicate) {
                _uiState.update { it.copy(error = "Já existe um item com esse nome", isLoading = false) }
                return@launch
            }

            val item = TripItem(
                id = itemId,
                tripId = tripId,
                name = displayName,
                category = state.category,
                requiredForDeparture = if (state.category == ItemCategory.GIFT) false else state.requiredForDeparture,
                requiredForReturn = if (state.category == ItemCategory.GIFT) false else state.requiredForReturn
            )

            if (itemId == null) {
                addItemUseCase(item)
            } else {
                updateItemUseCase(item)
            }

            _uiState.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }

    fun deleteItem(onSuccess: () -> Unit) {
        itemId?.let { id ->
            viewModelScope.launch {
                deleteItemUseCase(id)
                onSuccess()
            }
        }
    }

    private fun normalizeName(name: String): String {
        val noAccents = java.text.Normalizer.normalize(name.trim(), java.text.Normalizer.Form.NFD)
            .replace("\\p{M}".toRegex(), "")
        return noAccents.lowercase().replace("\\s+".toRegex(), " ")
    }
}