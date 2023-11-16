package com.advmeds.cliniccheckinapp.repositories

import android.content.Context
import androidx.room.Room
import com.advmeds.cliniccheckinapp.models.events.EventRepository
import com.advmeds.cliniccheckinapp.models.events.room.RoomEventRepository
import com.advmeds.cliniccheckinapp.models.room.AppDatabase

object RoomRepositories {

    private lateinit var applicationContext: Context

    private val database: AppDatabase by lazy<AppDatabase> {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database.db")
            .build()
    }

    // ---- repository
    val eventsRepository: EventRepository by lazy {
        RoomEventRepository(
            database.getEventDao(),
            database.getParamsDao()
        )
    }

    fun init(context: Context) {
        applicationContext = context
    }
}