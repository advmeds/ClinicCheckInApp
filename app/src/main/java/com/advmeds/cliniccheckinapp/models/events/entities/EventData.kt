package com.advmeds.cliniccheckinapp.models.events.entities

data class EventData(
    val id: Long = 0L,
    val eventName: String,
    val params: MutableMap<String, Any>
)