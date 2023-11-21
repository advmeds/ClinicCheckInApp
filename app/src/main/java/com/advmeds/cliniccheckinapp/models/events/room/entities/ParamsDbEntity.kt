package com.advmeds.cliniccheckinapp.models.events.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.advmeds.cardreadermodule.AcsResponseModel
import com.advmeds.cliniccheckinapp.dialog.EditCheckInItemDialog
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetPatientsResponse
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.AutomaticAppointmentSettingModel
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueueingMachineSettingModel
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueuingBoardSettingModel
import com.advmeds.cliniccheckinapp.utils.Converter
import com.google.gson.Gson
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
            val value = if (param.second is List<*>) {
                when ((param.second as List<*>).firstOrNull()) {
                    is String, Int -> (param.second as List<*>).joinToString(",")
                    is CreateAppointmentRequest.NationalIdFormat ->
                        Json.encodeToString(param.second as List<CreateAppointmentRequest.NationalIdFormat>)
                    else -> (param.second as List<*>).joinToString(",")
                }
            } else {
                when (param.second) {
                    is AcsResponseModel -> Gson().toJson(param.second)
                    is EditCheckInItemDialog.EditCheckInItems ->
                        Json.encodeToString(param.second as EditCheckInItemDialog.EditCheckInItems)
                    is QueuingBoardSettingModel ->
                        Json.encodeToString(param.second as QueuingBoardSettingModel)
                    is QueueingMachineSettingModel ->
                        Json.encodeToString(param.second as QueueingMachineSettingModel)
                    is AutomaticAppointmentSettingModel ->
                        Json.encodeToString(param.second as AutomaticAppointmentSettingModel)
                    is GetPatientsResponse ->
                        Json.encodeToString(param.second as GetPatientsResponse)
                    is Throwable -> Gson().toJson(param.second)
                    else -> param.second.toString()
                }
            }
            return ParamsDbEntity(
                eventId = eventId,
                paramKey = param.first,
                paramValue = value,
                paramType = Converter.determineType(param.second)
            )
        }
    }
}