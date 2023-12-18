package com.advmeds.cliniccheckinapp.models.events

import com.advmeds.cliniccheckinapp.models.events.entities.EventData
import com.advmeds.cliniccheckinapp.models.events.room.entities.SessionDbEntity

interface EventRepository {

    suspend fun getOrCreateNewSession(sessionNumber: Long, deviceId: Long): Long
    suspend fun getEventBySessionId(sessionId: Long): List<EventData>?
    suspend fun markSessionThatHaveBeenSentOnServer(sessionId: Long, wasSendOnServer: Boolean)
    suspend fun saveEventInDataBase(eventData: EventData): Long
    suspend fun getAllEventFromDatabase(): List<EventData>
    suspend fun getAllSessionsThatHaveNotSentOnServer(): List<SessionDbEntity>?
    suspend fun getAllSessionsThatHaveNotSentOnServerExceptCurrent(sessionId: Long): List<SessionDbEntity>?
    suspend fun deleteSessionThatHaveBeenSentOnServer()
    suspend fun getParamById(eventId: Long): Map<String, Any>

}