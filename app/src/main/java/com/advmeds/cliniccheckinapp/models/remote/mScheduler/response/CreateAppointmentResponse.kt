package com.advmeds.cliniccheckinapp.models.remote.mScheduler.response

import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateAppointmentResponse(
    /** is successful */
    val success: Boolean = false,

    /** error code */
    val code: Int = 0,

    /** error message */
    val message: String = "",

    /** doctor name */
    val doctor: String = "",

    /** division name */
    val division: String = "",

    /** division start time */
    @SerialName("starts_at")
    val startsAt: String = "",

    /** division end time */
    @SerialName("ends_at")
    val endsAt: String = "",

    /** patient information */
    val patient: CreateAppointmentRequest.Patient = CreateAppointmentRequest.Patient(),

    @SerialName("remote_link")
    val remoteLink: String = "",

    @SerialName("patient_remote_link")
    val patientRemoteLink: String = "",

    /** queue number */
    @SerialName("serial_num")
    val serialNo: Int = 0
)