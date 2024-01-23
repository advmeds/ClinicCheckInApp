package com.advmeds.cliniccheckinapp.models.remote.mScheduler.request

import android.content.Context
import com.advmeds.cliniccheckinapp.models.events.entities.EventData
import com.advmeds.cliniccheckinapp.models.events.entities.EventDataRequest
import com.advmeds.cliniccheckinapp.models.events.room.entities.SessionDbEntity
import com.advmeds.cliniccheckinapp.utils.Converter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CreateActionLogRequest(
    @SerialName("clinic_id")
    val clinicId: Int,
    val input: String,
    val title: String = "Check-in App",
    val content: List<SessionRequest>
)

@Serializable
data class SessionRequest(
    @SerialName("session_number")
    val sessionNumber: Long,
    @SerialName("device_id")
    val deviceId: Long,
    val events: List<EventDataRequest>?
) {
    companion object {
        private fun eventDataToRequest(
            eventData: EventData,
            context: Context? = null
        ): EventDataRequest {
            val requestParam = mutableMapOf<String, String>()

            for ((key, value) in eventData.params) {
                requestParam[key] = Converter.anyToString(value, context)
            }

            return EventDataRequest(
                eventName = eventData.eventName,
                params = requestParam
            )
        }

        fun fromMapToSessionRequest(
            sessions: List<SessionDbEntity>,
            sessionMap: Map<Long, List<EventData>>,
            context: Context? = null
        ): List<SessionRequest> {
            return sessions.map { session ->
                SessionRequest(
                    session.sessionNumber,
                    session.deviceId,
                    sessionMap[session.id]?.map { eventData ->
                        eventDataToRequest(
                            eventData = eventData,
                            context = context
                        )
                    }
                )
            }
        }
    }
}

