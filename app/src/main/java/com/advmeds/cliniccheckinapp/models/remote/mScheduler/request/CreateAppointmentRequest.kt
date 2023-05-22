package com.advmeds.cliniccheckinapp.models.remote.mScheduler.request

import com.advmeds.cliniccheckinapp.R
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

    /** 病患資訊的national_id可輸入什麼格式的資料 */
    enum class NationalIdFormat(private val pattern: String) {
        /** 身分證字號(預設) */
        DEFAULT("^[A-Z][A-Z\\d]\\d{8}\$"),

        /** 病歷號 */
        CASE_ID("");

        val description: Int
            get() = when(this) {
                DEFAULT -> R.string.national_id
                CASE_ID -> R.string.chart_no
            }

        fun inputFormatAvailable(input: String): Boolean {
            if (pattern.isBlank()) return true
            return Regex(pattern).matches(input)
        }
    }
}
