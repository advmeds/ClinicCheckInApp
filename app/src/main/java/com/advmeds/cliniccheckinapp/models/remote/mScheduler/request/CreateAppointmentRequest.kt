package com.advmeds.cliniccheckinapp.models.remote.mScheduler.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateAppointmentRequest(
    /** 診所代碼 */
    @SerialName("clinic_id")
    val clinicId: String = "",

    /** 檢查室代號 */
    val doctor: String = "",

    /** 醫生代號 */
    val division: String = "",

    /** 門診起始時間 */
    @SerialName("starts_at")
    val startsAt: String = "",

    /** 門診結束時間 */
    @SerialName("ends_at")
    val endsAt: String = "",

    /** 病患資訊 */
    val patient: Patient = Patient()
) {
    @Serializable
    data class Patient(
        /** 姓名 */
        val name: String = "",

        /** 生日 */
        val birthday: String = "",

        /** 手機號碼 */
        val mobile: String = "",

        /** 身分證字號 */
        @SerialName("national_id")
        val nationalId: String = "",
    )
}