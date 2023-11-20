package com.advmeds.cliniccheckinapp.ui

import com.advmeds.cardreadermodule.AcsResponseModel
import com.advmeds.cliniccheckinapp.repositories.AnalyticsRepository
import com.advmeds.cliniccheckinapp.repositories.AnalyticsRepository.DestinationType

class MainEventLogger(
    private val analyticsRepository: AnalyticsRepository
) {

    suspend fun logUserInsertTheCard(result: Result<AcsResponseModel>) {
        val map = mutableMapOf<String, Any>()
        map[AnalyticsRepository.SOURCE_SCREEN] = "Main Activity"
        map[AnalyticsRepository.SOURCE_ACTION] = "user_insert_card"

        result.onSuccess {
            map["insert_card_result"] = "onSuccess"
            map["insert_card_data"] = it
        }.onFailure {
            map["insert_card_result"] = "onFailure"
            map["insert_card_data"] = it
        }

        analyticsRepository.sendEvent(
            eventName = "user_insert_card",
            params = map,
            destination = DestinationType.LOCAL
        )
        analyticsRepository.sendEvent(
            eventName = "",
            destination = DestinationType.LOCAL_TO_SERVER
        )
    }
}