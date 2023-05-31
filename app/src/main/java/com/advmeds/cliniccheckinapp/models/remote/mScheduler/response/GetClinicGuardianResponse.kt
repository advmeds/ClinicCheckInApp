package com.advmeds.cliniccheckinapp.models.remote.mScheduler.response

import kotlinx.serialization.Serializable

@Serializable
data class GetClinicGuardianResponse(
    /** is successful */
    val success: Boolean = false,

    /** error code */
    val code: Int = 0,

    /** error message */
    val message: String = "",

    /** clinic name */
    val name: String = "",

    /** clinic LOGO */
    val logo: String = "",

    /** marquee */
    val marquee: String = "",

    /** language */
    val language: String = ""
)