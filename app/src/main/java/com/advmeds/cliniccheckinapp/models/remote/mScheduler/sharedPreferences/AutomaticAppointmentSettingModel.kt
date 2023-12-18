package com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences

import kotlinx.serialization.Serializable

@Serializable
data class AutomaticAppointmentSettingModel(
    val isEnabled: Boolean,
    val mode: AutomaticAppointmentMode,
    val doctorId: String,
    val roomId: String,
    val autoCheckIn: Boolean = true
)

@Serializable
enum class AutomaticAppointmentMode {
    SINGLE_MODE,
    MULTIPLE_MODE
}