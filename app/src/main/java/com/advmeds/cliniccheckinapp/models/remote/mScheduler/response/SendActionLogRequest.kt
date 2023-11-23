package com.advmeds.cliniccheckinapp.models.remote.mScheduler.response

import kotlinx.serialization.Serializable

@Serializable
data class SendActionLogRequest(
    /** is successful */
    val success: Boolean = false,

    /** error code */
    val code: Int = 0,

    /** error message */
    val message: String = "",
)
