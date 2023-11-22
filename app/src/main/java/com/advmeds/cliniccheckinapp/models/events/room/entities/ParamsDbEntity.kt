package com.advmeds.cliniccheckinapp.models.events.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.advmeds.cliniccheckinapp.utils.Converter

@Entity(
    tableName = "event_params",
    foreignKeys = [
        ForeignKey(
            entity = EventDbEntity::class,
            parentColumns = ["id"],
            childColumns = ["event_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class ParamsDbEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "event_id") val eventId: Long,
    @ColumnInfo(name = "param_key") val paramKey: String,
    @ColumnInfo(name = "param_value") val paramValue: String,
    @ColumnInfo(name = "param_type") val paramType: String? = null
) {

    fun toPair(): Pair<String, Any> = Pair(
        paramKey,
        Converter.convertToCorrectType(paramValue, paramType)
    )

    companion object {
        fun fromMapToParam(eventId: Long, param: Pair<String, Any>): ParamsDbEntity {
            val value = Converter.anyToString(param.second)
            return ParamsDbEntity(
                eventId = eventId,
                paramKey = param.first,
                paramValue = value,
                paramType = Converter.determineType(param.second)
            )
        }
    }
}