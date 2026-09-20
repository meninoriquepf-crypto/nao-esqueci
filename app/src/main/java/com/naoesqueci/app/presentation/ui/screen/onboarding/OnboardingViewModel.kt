package com.naoesqueci.app.presentation.ui.screen.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel

class OnboardingViewModel(private val context: Context) : ViewModel() {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun isFirstRun(): Boolean {
        return !prefs.getBoolean("has_seen_onboarding", false)
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("has_seen_onboarding", true).apply()
    }
}