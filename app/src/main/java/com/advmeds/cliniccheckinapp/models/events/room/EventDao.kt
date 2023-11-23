package com.advmeds.cliniccheckinapp.models.events.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.advmeds.cliniccheckinapp.models.events.room.entities.EventDbEntity

@Dao
interface EventDao {

    @Insert(entity = EventDbEntity::class, onConflict = OnConflictStrategy.IGNORE)
    suspend fun createEvent(eventDbEntity: EventDbEntity) : Long

    @Query("SELECT * FROM events")
    suspend fun getAllEvents(): List<EventDbEntity>?

    @Query("SELECT * FROM events WHERE session_id = :sessionId")
    suspend fun getEventBySessionId(sessionId: Long): List<EventDbEntity>?

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: Long) : EventDbEntity
}