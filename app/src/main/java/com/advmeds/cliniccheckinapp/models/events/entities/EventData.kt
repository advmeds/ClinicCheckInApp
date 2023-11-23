package com.advmeds.cliniccheckinapp.models.events.entities

data class EventData(
    val id: Long = 0L,
    val sessionId: Long,
    val eventName: String,
    val params: MutableMap<String, Any>
)