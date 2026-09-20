package com.naoesqueci.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsKeys {
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val ADVANCE_MINUTES = intPreferencesKey("advance_minutes")
    val THEME = stringPreferencesKey("theme")
}

class SettingsDataStore(private val context: Context) {

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[SettingsKeys.NOTIFICATIONS_ENABLED] ?: true }

    val advanceMinutes: Flow<Int> = context.dataStore.data
        .map { it[SettingsKeys.ADVANCE_MINUTES] ?: 30 }

    val theme: Flow<String> = context.dataStore.data
        .map { it[SettingsKeys.THEME] ?: "system" }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SettingsKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setAdvanceMinutes(minutes: Int) {
        context.dataStore.edit { it[SettingsKeys.ADVANCE_MINUTES] = minutes }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[SettingsKeys.THEME] = theme }
    }
}