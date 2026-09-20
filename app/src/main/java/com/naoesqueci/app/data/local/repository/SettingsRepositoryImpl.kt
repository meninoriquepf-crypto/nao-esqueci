package com.naoesqueci.app.data.local.repository

import com.naoesqueci.app.data.local.preferences.SettingsDataStore
import com.naoesqueci.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(private val settingsDataStore: SettingsDataStore) : SettingsRepository {
    override val notificationsEnabled: Flow<Boolean> = settingsDataStore.notificationsEnabled
    override val advanceMinutes: Flow<Int> = settingsDataStore.advanceMinutes
    override val repeatMinutes: Flow<Int> = settingsDataStore.repeatMinutes
    override val theme: Flow<String> = settingsDataStore.theme
    override suspend fun setNotificationsEnabled(enabled: Boolean) = settingsDataStore.setNotificationsEnabled(enabled)
    override suspend fun setAdvanceMinutes(minutes: Int) = settingsDataStore.setAdvanceMinutes(minutes)
    override suspend fun setRepeatMinutes(minutes: Int) = settingsDataStore.setRepeatMinutes(minutes)
    override suspend fun setTheme(theme: String) = settingsDataStore.setTheme(theme)
}