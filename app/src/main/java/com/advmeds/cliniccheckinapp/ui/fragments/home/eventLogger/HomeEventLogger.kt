package com.advmeds.cliniccheckinapp.ui.fragments.home.eventLogger

import com.advmeds.cliniccheckinapp.repositories.AnalyticsRepository

class HomeEventLogger(
    private val analyticsRepository: AnalyticsRepository
) {
    suspend fun logUserOpenSettingsScreen() {
        val map = mutableMapOf<String, Any>()
        map[AnalyticsRepository.SOURCE_SCREEN] = "home screen"
        map[AnalyticsRepository.SOURCE_ACTION] = "open setting screen"
        analyticsRepository.sendEvent("open_setting_screen", map)
    }

    suspend fun logUserClickCustomizedButton() {
        val map = mutableMapOf<String, Any>()
        map[AnalyticsRepository.SOURCE_SCREEN] = "home screen"
        analyticsRepository.sendEvent("user_click_customized_button", map)
    }
}