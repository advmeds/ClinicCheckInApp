package com.advmeds.cliniccheckinapp.models.events.room

import com.advmeds.cliniccheckinapp.models.events.EventRepository
import com.advmeds.cliniccheckinapp.models.events.entities.EventData
import com.advmeds.cliniccheckinapp.models.events.room.entities.EventDbEntity
import com.advmeds.cliniccheckinapp.models.events.room.entities.ParamsDbEntity
import com.advmeds.cliniccheckinapp.models.events.room.entities.SessionDbEntity
import com.advmeds.cliniccheckinapp.models.events.room.entities.SessionUpdateWasSendOnServerTuple

class RoomEventRepository(
    private val eventDao: EventDao,
    private val paramsDao: ParamsDao,
    private val sessionDao: SessionDao
) : EventRepository {

    override suspend fun getOrCreateNewSession(sessionNumber: Long, deviceId: Long): Long {
        val sessionTuples = sessionDao.findSessionIdBySessionNumber(sessionNumber)

        if (sessionTuples != null)
            return sessionTuples.id

        val entity = SessionDbEntity.toSessionDbEntity(sessionNumber, deviceId)
        return sessionDao.createSession(entity)
    }

    override suspend fun saveEventInDataBase(eventData: EventData): Long {
        val eventEntity = EventDbEntity.fromSendEvent(eventData)
        val id = eventDao.createEvent(eventEntity)

        for ((key, value) in eventData.params) {
            paramsDao.addNewParam(
                ParamsDbEntity.fromMapToParam(id, Pair(key, value))
            )
        }

        return id
    }


    override suspend fun getAllEventFromDatabase(): List<EventData> {
        return eventDao.getAllEvents()?.map { eventDbEntity -> eventDbEntity.toEvent() } ?: listOf()
    }

    override suspend fun getEventBySessionId(sessionId: Long): List<EventData>? {
        return eventDao.getEventBySessionId(sessionId)
            ?.map { eventDbEntity -> eventDbEntity.toEvent() }
    }

    override suspend fun markSessionThatHaveBeenSentOnServer(
        sessionId: Long,
        wasSendOnServer: Boolean
    ) {
        sessionDao.updateWasSendOnServer(
            SessionUpdateWasSendOnServerTuple(
                sessionId,
                wasSendOnServer
            )
        )
    }

    override suspend fun getAllSessionsThatHaveNotSentOnServer(): List<SessionDbEntity>? {
        return sessionDao.getAllSessionsThatHaveNotSentOnServer()
    }

    override suspend fun getAllSessionsThatHaveNotSentOnServerExceptCurrent(sessionId: Long): List<SessionDbEntity>? {
        return sessionDao.getAllSessionsThatHaveNotSentOnExceptCurrentServer(sessionId)
    }

    override suspend fun deleteSessionThatHaveBeenSentOnServer() {
        sessionDao.deleteSentSession()
    }

    override suspend fun getParamById(eventId: Long): Map<String, Any> {
        return paramsDao.findParamsByEvent(eventId)?.associate { paramsDbEntity ->
            paramsDbEntity.toPair()
        } ?: mapOf()
    }
}