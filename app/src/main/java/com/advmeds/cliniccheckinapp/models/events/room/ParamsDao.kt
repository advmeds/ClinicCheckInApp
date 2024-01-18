package com.advmeds.cliniccheckinapp.models.events.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.advmeds.cliniccheckinapp.models.events.room.entities.ParamsDbEntity

@Dao
interface ParamsDao {
    @Query("SELECT * FROM event_params WHERE event_id = :eventId")
    suspend fun findParamsByEvent(eventId: Long): List<ParamsDbEntity>?

    @Insert
    suspend fun addNewParam(param: ParamsDbEntity)
}