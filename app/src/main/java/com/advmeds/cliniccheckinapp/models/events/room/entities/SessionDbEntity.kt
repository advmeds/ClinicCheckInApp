package com.advmeds.cliniccheckinapp.models.events.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "session",
    indices = [
        Index("session_number", unique = true)
    ]
)
data class SessionDbEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "session_number") val sessionNumber: Long,
    @ColumnInfo(name = "device_id") val deviceId: Long,
    @ColumnInfo(name = "was_send_on_server") val wasSendOnServer: Boolean = false
) {

    companion object {
        fun toSessionDbEntity(sessionNumber: Long, deviceId: Long) = SessionDbEntity(
            id = 0,
            sessionNumber = sessionNumber,
            deviceId = deviceId,
            wasSendOnServer = false
        )
    }
}