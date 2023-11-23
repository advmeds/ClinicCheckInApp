package com.advmeds.cliniccheckinapp.models.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.advmeds.cliniccheckinapp.models.events.room.EventDao
import com.advmeds.cliniccheckinapp.models.events.room.ParamsDao
import com.advmeds.cliniccheckinapp.models.events.room.SessionDao
import com.advmeds.cliniccheckinapp.models.events.room.entities.EventDbEntity
import com.advmeds.cliniccheckinapp.models.events.room.entities.ParamsDbEntity
import com.advmeds.cliniccheckinapp.models.events.room.entities.SessionDbEntity

@Database(
    version = 1,
    entities = [
        SessionDbEntity::class,
        EventDbEntity::class,
        ParamsDbEntity::class
    ]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getSessionDao(): SessionDao

    abstract fun getEventDao(): EventDao

    abstract fun getParamsDao(): ParamsDao
}