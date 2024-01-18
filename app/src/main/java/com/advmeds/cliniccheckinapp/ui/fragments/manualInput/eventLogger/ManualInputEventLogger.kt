package com.advmeds.cliniccheckinapp.ui.fragments.manualInput.eventLogger

import com.advmeds.cliniccheckinapp.repositories.AnalyticsRepository

class ManualInputEventLogger(
    private val analyticsRepository: AnalyticsRepository
) {

    suspend fun logUserSendTheManualInputData(manualInputData: String) {
        val map = mutableMapOf<String, Any>()
        map[AnalyticsRepository.SOURCE_SCREEN] = "manual input screen"
        map[AnalyticsRepository.SOURCE_ACTION] = "send the manual input data"

        map["manual input data"] = manualInputData

        analyticsRepository.sendEvent("send the manual input data", map)
    }
}