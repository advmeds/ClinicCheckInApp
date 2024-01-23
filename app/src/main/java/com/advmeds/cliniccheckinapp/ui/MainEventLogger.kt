package com.advmeds.cliniccheckinapp.ui

import android.content.Context
import com.advmeds.cardreadermodule.AcsResponseModel
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.CreateAppointmentResponse
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetPatientsResponse
import com.advmeds.cliniccheckinapp.repositories.AnalyticsRepository
import com.advmeds.cliniccheckinapp.repositories.ServerRepository

class MainEventLogger(
    private val analyticsRepository: AnalyticsRepository
) {

    fun setServerRepoInAnalyticsRepository(serverRepository: ServerRepository) {
        analyticsRepository.setServerRepository(serverRepository)
    }

    suspend fun sendLogsFromLocalToServer(context: Context? = null) {
        analyticsRepository.sendEvent(
            eventName = "send logs from local to server",
            destination = AnalyticsRepository.DestinationType.LOCAL_TO_SERVER,
            context = context
        )
    }

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

        analyticsRepository.sendEvent(eventName = "user_insert_card", params = map)
    }

    suspend fun logAppOpen(
        closeAppEvent: Pair<String, Map<String, Any>>? = null,
        sessionNumber: Int
    ) {
        if (closeAppEvent != null) {
            val map = mutableMapOf<String, Any>()
            map[AnalyticsRepository.SOURCE_SCREEN] = "Main Application"
            map[AnalyticsRepository.SOURCE_ACTION] = "The app is closing"

            map.putAll(closeAppEvent.second)

            analyticsRepository.sendEvent(eventName = closeAppEvent.first, map, sessionNumber = (sessionNumber - 1).toLong())

        }

        val map = mutableMapOf<String, Any>()
        map[AnalyticsRepository.SOURCE_SCREEN] = "Main Activity"
        map[AnalyticsRepository.SOURCE_ACTION] = "The app is opening"

        analyticsRepository.sendEvent(eventName = "app is opening", map)
    }

    suspend fun logAppPrintsATicket(
        divisions: List<String>,
        serialNumbers: List<Int>,
        doctors: List<String>
    ) {
        val map = mutableMapOf<String, Any>()
        map[AnalyticsRepository.SOURCE_SCREEN] = "Main Activity"
        map[AnalyticsRepository.SOURCE_ACTION] = "app_prints_ticket"

        map["divisions"] = divisions
        map["serialNumbers"] = serialNumbers
        map["doctors"] = doctors

        analyticsRepository.sendEvent(eventName = "app prints ticket", map)
    }

    suspend fun logResponseGetPatient(response: GetPatientsResponse) {
        val map = mutableMapOf<String, Any>()
        map[AnalyticsRepository.SOURCE_SCREEN] = "Main Activity"
        map[AnalyticsRepository.SOURCE_ACTION] = "server response for get patient request"

        map["get patient result"] = response

        analyticsRepository.sendEvent("response: Get Patient", map)
    }

    suspend fun logAppCreateAppointment(request: CreateAppointmentRequest) {
        val map = mutableMapOf<String, Any>()
        map[AnalyticsRepository.SOURCE_SCREEN] = "Main Activity"
        map[AnalyticsRepository.SOURCE_ACTION] = "app start making create appointment request"

        map["request"] = request

        analyticsRepository.sendEvent("request: Create Appointment", map)
    }

    suspend fun logResponseCreateAppointment(response: CreateAppointmentResponse) {
        val map = mutableMapOf<String, Any>()
        map[AnalyticsRepository.SOURCE_SCREEN] = "Main Activity"
        map[AnalyticsRepository.SOURCE_ACTION] = "server response for create appointment request"

        map["response"] = response

        analyticsRepository.sendEvent("response: Create Appointment", map)
    }
}