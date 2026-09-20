package com.naoesqueci.app.presentation.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naoesqueci.app.domain.usecase.settings.GetAdvanceTimeUseCase
import com.naoesqueci.app.domain.usecase.settings.GetNotificationsEnabledUseCase
import com.naoesqueci.app.domain.usecase.settings.GetThemeUseCase
import com.naoesqueci.app.domain.usecase.settings.UpdateAdvanceTimeUseCase
import com.naoesqueci.app.domain.usecase.settings.UpdateNotificationsEnabledUseCase
import com.naoesqueci.app.domain.usecase.settings.UpdateThemeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    getThemeUseCase: GetThemeUseCase,
    private val updateThemeUseCase: UpdateThemeUseCase,
    getNotificationsEnabledUseCase: GetNotificationsEnabledUseCase,
    private val updateNotificationsEnabledUseCase: UpdateNotificationsEnabledUseCase,
    getAdvanceTimeUseCase: GetAdvanceTimeUseCase,
    private val updateAdvanceTimeUseCase: UpdateAdvanceTimeUseCase
) : ViewModel() {

    data class UiState(
        val selectedTheme: String = "system",
        val notificationsEnabled: Boolean = true,
        val advanceMinutes: Int = 30
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        viewModelScope.launch {
            getThemeUseCase().collect { theme ->
                _uiState.update { it.copy(selectedTheme = theme) }
            }
        }
        viewModelScope.launch {
            getNotificationsEnabledUseCase().collect { enabled ->
                _uiState.update { it.copy(notificationsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            getAdvanceTimeUseCase().collect { minutes ->
                _uiState.update { it.copy(advanceMinutes = minutes) }
            }
        }
    }

    fun setTheme(theme: String) {
        _uiState.update { it.copy(selectedTheme = theme) }
        viewModelScope.launch { updateThemeUseCase(theme) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        viewModelScope.launch { updateNotificationsEnabledUseCase(enabled) }
    }

    fun setAdvanceMinutes(minutes: Int) {
        _uiState.update { it.copy(advanceMinutes = minutes) }
        viewModelScope.launch { updateAdvanceTimeUseCase(minutes) }
    }
}