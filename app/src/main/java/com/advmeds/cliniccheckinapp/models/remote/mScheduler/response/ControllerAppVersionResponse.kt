package com.advmeds.cliniccheckinapp.models.remote.mScheduler.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ControllerAppVersionResponse(
    val success: Boolean,

    @SerialName("is_available")
    val isAvailable: Boolean = false,

    val version: String = "",

    val url: String = "",

    val code: Int = 0,

    val message: String = ""

)