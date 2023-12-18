package com.advmeds.cliniccheckinapp.models.events.sharedpreference

import com.advmeds.cliniccheckinapp.utils.Converter
import kotlinx.serialization.Serializable

@Serializable
data class CloseAppEventModel(
    val eventName: String,
    val params: Map<String, String>,
    val type:  Map<String, String>
) {

    fun fromModelToPair() : Pair<String, Map<String, Any>> {
        val eventName = eventName

        val paramsAny = mutableMapOf<String, Any>()

        for ((key, value) in params) {
            val param = Converter.convertToCorrectType(value, type[key])
            paramsAny[key] = param
        }

        return Pair(eventName, paramsAny)
    }

    companion object {
        fun fromMapToModel(eventName: String, params: Map<String, Any>) : CloseAppEventModel {
            val paramsString = mutableMapOf<String, String>()
            val paramType = mutableMapOf<String, String>()

            for ((key, value) in params) {
                val stringValue = Converter.anyToString(value)
                val type = Converter.determineType(value)

                paramsString[key] = stringValue
                paramType[key] = type
            }

            return CloseAppEventModel(
                eventName = eventName,
                params = paramsString,
                type = paramType
            )
        }
    }
}