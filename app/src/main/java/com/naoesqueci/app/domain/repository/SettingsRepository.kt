package com.naoesqueci.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val notificationsEnabled: Flow<Boolean>
    val advanceMinutes: Flow<Int>
    val theme: Flow<String>
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setAdvanceMinutes(minutes: Int)
    suspend fun setTheme(theme: String)
}