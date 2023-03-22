package com.advmeds.cliniccheckinapp.models.remote.mScheduler.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateAppointmentShortRequest(
    /** 診所代碼 */
    @SerialName("clinic_id")
    val clinicId: String = "",

    /** 檢查室代號 */
    val doctor: String = "",

    /** 醫生代號 */
    val division: String = "",

    /** 病患資訊 */
    val patient: CreateAppointmentRequest.Patient = CreateAppointmentRequest.Patient()
)

