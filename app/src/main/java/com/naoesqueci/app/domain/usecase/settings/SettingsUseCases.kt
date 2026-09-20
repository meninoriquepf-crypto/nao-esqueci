package com.naoesqueci.app.domain.usecase.settings

import com.naoesqueci.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationsEnabledUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<Boolean> = repository.notificationsEnabled
}

class UpdateNotificationsEnabledUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setNotificationsEnabled(enabled)
}

class GetAdvanceTimeUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<Int> = repository.advanceMinutes
}

class UpdateAdvanceTimeUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(minutes: Int) = repository.setAdvanceMinutes(minutes)
}

class GetRepeatMinutesUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<Int> = repository.repeatMinutes
}

class UpdateRepeatMinutesUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(minutes: Int) = repository.setRepeatMinutes(minutes)
}

class GetThemeUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<String> = repository.theme
}

class UpdateThemeUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(theme: String) = repository.setTheme(theme)
}