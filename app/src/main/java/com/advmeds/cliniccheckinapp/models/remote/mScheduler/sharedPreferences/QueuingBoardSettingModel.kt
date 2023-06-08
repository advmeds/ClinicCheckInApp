package com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences

import kotlinx.serialization.Serializable

@Serializable
data class QueuingBoardSettingModel(
    val isEnabled: Boolean,
    val url: String,
)