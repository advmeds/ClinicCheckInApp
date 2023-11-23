package com.advmeds.cliniccheckinapp.models.events.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


data class EventData(
    val id: Long = 0L,
    val sessionId: Long,
    val eventName: String,
    val params: MutableMap<String, Any>
)

@Serializable
data class EventDataRequest(
    @SerialName("event_name")
    val eventName: String,
    val params: MutableMap<String, String>
)