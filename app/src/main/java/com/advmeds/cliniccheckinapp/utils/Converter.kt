package com.advmeds.cliniccheckinapp.utils

import android.content.Context
import com.advmeds.cardreadermodule.AcsResponseModel
import com.advmeds.cliniccheckinapp.BuildConfig
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.dialog.EditCheckInItemDialog
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.CreateAppointmentResponse
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetPatientsResponse
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.AutomaticAppointmentSettingModel
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueueingMachineSettingModel
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueuingBoardSettingModel
import com.google.gson.Gson
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object Converter {
    fun language_lang_code_to_name(
        context: Context,
        langCode: String = BuildConfig.DEFAULT_LANGUAGE
    ): String {
        return when (langCode) {
            "en" -> context.getString(R.string.en_language)
            "zh" -> context.getString(R.string.zh_tw_language)
            else -> throw IllegalArgumentException("The language code has no similar languages")
        }
    }

    fun language_name_to_lang_code(context: Context, name: String): String {
        return when (name) {
            context.getString(R.string.en_language) -> "en"
            context.getString(R.string.zh_tw_language) -> "zh"
            else -> throw IllegalArgumentException("The language has no similar language codes")
        }
    }

    fun determineType(value: Any): String? {
        if (value is List<*>) {
            return when ((value as List<*>).firstOrNull()) {
                is CreateAppointmentRequest.NationalIdFormat ->
                    "List<CreateAppointmentRequest.NationalIdFormat>"
                is String -> "List<String>"
                is Int -> "List<Int>"
                else -> "List<String>"
            }
        }

        return when (value) {
            is String -> "String"
            is Int -> "Int"
            is Double -> "Double"
            is AcsResponseModel -> "AcsResponseModel"
            is EditCheckInItemDialog.EditCheckInItems -> "EditCheckInItemDialog.EditCheckInItems"
            is QueuingBoardSettingModel -> "QueuingBoardSettingModel"
            is QueueingMachineSettingModel -> "QueueingMachineSettingModel"
            is AutomaticAppointmentSettingModel -> "AutomaticAppointmentSettingModel"
            is GetPatientsResponse -> "GetPatientsResponse"
            is EditCheckInItemDialog.EditCheckInItem -> "EditCheckInItemDialog.EditCheckInItem"
            is CreateAppointmentRequest -> "CreateAppointmentRequest"
            is CreateAppointmentResponse -> "CreateAppointmentResponse"
            is Throwable -> "Throwable"
            else -> "String"
        }
    }

    fun convertToCorrectType(value: String, type: String?): Any {
        return when (type) {
            "Int" -> value.toIntOrNull() ?: value
            "Double" -> value.toDoubleOrNull() ?: value
            "AcsResponseModel" -> Gson().fromJson(value, AcsResponseModel::class.java)
            "EditCheckInItemDialog.EditCheckInItems" ->
                Json.decodeFromString<EditCheckInItemDialog.EditCheckInItems>(value)
            "QueuingBoardSettingModel" ->
                Json.decodeFromString<QueuingBoardSettingModel>(value)
            "QueueingMachineSettingModel" ->
                Json.decodeFromString<QueueingMachineSettingModel>(value)
            "AutomaticAppointmentSettingModel" ->
                Json.decodeFromString<AutomaticAppointmentSettingModel>(value)
            "GetPatientsResponse" ->
                Json.decodeFromString<GetPatientsResponse>(value)
            "EditCheckInItemDialog.EditCheckInItem" ->
                Json.decodeFromString<EditCheckInItemDialog.EditCheckInItem>(value)
            "CreateAppointmentRequest" ->
                Json.decodeFromString<CreateAppointmentRequest>(value)
            "CreateAppointmentResponse" ->
                Json.decodeFromString<CreateAppointmentResponse>(value)
            "Throwable" -> Gson().fromJson(value, Throwable::class.java)
            "List<CreateAppointmentRequest.NationalIdFormat>" ->
                Json.decodeFromString(
                    ListSerializer(CreateAppointmentRequest.NationalIdFormat.serializer()),
                    value
                )
            "List<String>" -> value.split(",").filter { it.isNotBlank() }
            "List<Int>" -> value.split(",").map { it.toInt() }
            else -> value
        }
    }
}