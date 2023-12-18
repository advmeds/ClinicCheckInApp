package com.advmeds.cliniccheckinapp.models.events.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.advmeds.cliniccheckinapp.models.events.entities.EventData

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = SessionDbEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class EventDbEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "session_id") val sessionId: Long = 0,
    @ColumnInfo(name = "event_name") val eventName: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
) {

    fun toEvent(): EventData = EventData(
        id = id,
        sessionId = sessionId,
        eventName = eventName,
        params = mutableMapOf()
    )

    companion object {
        fun fromSendEvent(eventData: EventData): EventDbEntity = EventDbEntity(
            id = 0,
            sessionId =eventData.sessionId,
            eventName = eventData.eventName,
            createdAt = System.currentTimeMillis()
        )
    }
}