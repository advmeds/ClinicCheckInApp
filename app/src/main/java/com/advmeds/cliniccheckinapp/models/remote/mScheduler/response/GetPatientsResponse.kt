package com.advmeds.cliniccheckinapp.models.remote.mScheduler.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPatientsResponse(
    /** API 執行是否成功 */
    val success: Boolean = false,

    /** 錯誤代碼 */
    val code: Int = 0,

    /** 錯誤訊息 */
    val message: String = "",

    /** 病患資料 */
    val patients: List<PatientBean> = emptyList()
) {
    @Serializable
    data class PatientBean(
        /** 醫師 */
        val doctor: String = "",

        /** 科別 */
        val division: String = "",

        /** 身分證 */
        @SerialName("national_id")
        val nationalId: String = "",

        /** 病歷號 */
        @SerialName("patient_id")
        val patientId: String = "",

        /** 姓名 */
        val name: String = "",

        /** 生日 */
        val birthday: String = "",

        /** 備註 */
        val note: String = "",

        /** 看診號碼 */
        @SerialName("serial_num")
        val serialNo: Int = 0
    )
}