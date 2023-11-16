package com.advmeds.cliniccheckinapp.models.events

import com.advmeds.cliniccheckinapp.models.events.entities.EventData

interface EventRepository {
    suspend fun saveEventInDataBase(eventData: EventData) : Long
    suspend fun getAllEventFromDatabase(): List<EventData>
    suspend fun getParamById(eventId: Long): Map<String, Any>

}