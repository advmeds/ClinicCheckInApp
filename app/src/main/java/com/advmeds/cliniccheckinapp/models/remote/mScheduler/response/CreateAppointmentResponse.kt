package com.advmeds.cliniccheckinapp.models.remote.mScheduler.response

import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateAppointmentResponse(
    /** API 執行是否成功 */
    val success: Boolean = false,

    /** 錯誤代碼 */
    val code: Int = 0,

    /** 錯誤訊息 */
    val message: String = "",

    /** 醫師 */
    val doctor: String = "",

    /** 科別 */
    val division: String = "",

    /** 門診起始時間 */
    @SerialName("starts_at")
    val startsAt: String = "",

    /** 門診結束時間 */
    @SerialName("ends_at")
    val endsAt: String = "",

    /** 病患資訊 */
    val patient: CreateAppointmentRequest.Patient = CreateAppointmentRequest.Patient(),

    @SerialName("remote_link")
    val remoteLink: String = "",

    @SerialName("patient_remote_link")
    val patientRemoteLink: String = "",

    /** 看診號碼 */
    @SerialName("serial_num")
    val serialNo: Int = 0
)