package com.advmeds.cliniccheckinapp.models.events.room.entities

import androidx.room.ColumnInfo

data class SessionTuples(
    val id: Long
)

data class SessionUpdateWasSendOnServerTuple(
    val id: Long,
    @ColumnInfo(name = "was_send_on_server") val wasSendOnServer: Boolean
)