package com.advmeds.cliniccheckinapp.models.events.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.advmeds.cliniccheckinapp.models.events.room.entities.SessionDbEntity
import com.advmeds.cliniccheckinapp.models.events.room.entities.SessionTuples
import com.advmeds.cliniccheckinapp.models.events.room.entities.SessionUpdateWasSendOnServerTuple


@Dao
interface SessionDao {

    @Query("SELECT id FROM session WHERE session_number = :sessionNumber")
    suspend fun findSessionIdBySessionNumber(sessionNumber: Long): SessionTuples?

    @Update(entity = SessionDbEntity::class)
    suspend fun updateWasSendOnServer(updateWasSendOnServerTuple: SessionUpdateWasSendOnServerTuple)

    @Insert
    suspend fun createSession(session: SessionDbEntity) : Long

    @Query("DELETE FROM session WHERE was_send_on_server = 1")
    suspend fun deleteSentSession()
}