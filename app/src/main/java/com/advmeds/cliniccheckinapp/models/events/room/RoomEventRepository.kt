package com.advmeds.cliniccheckinapp.models.events.room

import com.advmeds.cliniccheckinapp.models.events.EventRepository
import com.advmeds.cliniccheckinapp.models.events.entities.EventData
import com.advmeds.cliniccheckinapp.models.events.room.entities.EventDbEntity
import com.advmeds.cliniccheckinapp.models.events.room.entities.ParamsDbEntity

class RoomEventRepository(
    private val eventDao: EventDao,
    private val paramsDao: ParamsDao
) : EventRepository {

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

    override suspend fun getParamById(eventId: Long): Map<String, Any> {
        return paramsDao.findParamsByEvent(eventId)?.associate { paramsDbEntity ->
            paramsDbEntity.toPair()
        } ?: mapOf()
    }
}