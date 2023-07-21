package com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences

import kotlinx.serialization.Serializable

@Serializable
data class AutomaticAppointmentSettingModel(
    val isEnabled: Boolean,
    val doctorId: String,
    val roomId: String,
    val autoCheckIn: Boolean
)