package com.naoesqueci.app

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.naoesqueci.app.data.local.preferences.SettingsDataStore
import com.naoesqueci.app.data.local.repository.SettingsRepositoryImpl
import com.naoesqueci.app.domain.usecase.settings.GetThemeUseCase
import com.naoesqueci.app.domain.usecase.settings.UpdateThemeUseCase
import com.naoesqueci.app.alarm.AlarmConstants
import com.naoesqueci.app.domain.model.TripType
import com.naoesqueci.app.presentation.navigation.AppNavHost
import com.naoesqueci.app.presentation.ui.theme.DynamicTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {

    private val _deepLinkTripId = MutableStateFlow<Long?>(null)
    private val deepLinkTripId: StateFlow<Long?> = _deepLinkTripId.asStateFlow()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsDataStore = SettingsDataStore(this)
        val settingsRepository = SettingsRepositoryImpl(settingsDataStore)
        val getThemeUseCase = GetThemeUseCase(settingsRepository)
        val updateThemeUseCase = UpdateThemeUseCase(settingsRepository)

        setContent {
            DynamicTheme(
                getThemeUseCase = getThemeUseCase,
                updateThemeUseCase = updateThemeUseCase
            ) {
                MaterialTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavHost(
                            deepLinkTripId = deepLinkTripId,
                            onDeepLinkConsumed = { _deepLinkTripId.value = null }
                        )
                    }
                }
            }
        }

        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val tripId = intent.getLongExtra("DEEP_LINK_TRIP_ID", -1)
        _deepLinkTripId.value = if (tripId == -1L) null else tripId
        if (tripId != -1L) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(AlarmConstants.finalNotificationId(tripId, TripType.DEPARTURE.ordinal))
            manager.cancel(AlarmConstants.finalNotificationId(tripId, TripType.RETURN.ordinal))
        }
    }
}