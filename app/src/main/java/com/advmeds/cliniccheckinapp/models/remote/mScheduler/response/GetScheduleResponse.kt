package com.advmeds.cliniccheckinapp.models.remote.mScheduler.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class GetScheduleResponse(
    /** API 執行是否成功 */
    val success: Boolean = false,

    /** 錯誤代碼 */
    val code: Int = 0,

    /** 錯誤訊息 */
    val message: String = "",

    /** 病患資料 */
    val schedules: List<ScheduleBean> = emptyList()
) {
    @Serializable
    data class ScheduleBean(
        /** 醫生代號 */
        val doctor: String = "",

        /** 醫生名稱 */
        @SerialName("doctor_name")
        val doctorName: String = "",

        /** 檢查室代號 */
        val division: String = "",

        /** 檢查室名稱 */
        @SerialName("division_name")
        val divisionName: String = "",

        /** 病患預約名額上限 */
        @SerialName("patient_quota")
        val patientQuota: Int = 0,

        /** 門診起始時間 */
        @SerialName("starts_at")
        val startsAt: String = "",

        /** 門診結束時間 */
        @SerialName("ends_at")
        val endsAt: String = "",
    ) {
        companion object {
            /** 屏基小兒心超 */
            val PTCH_BABY = ScheduleBean(
                doctor = "CA",
                division = "0000"
            )

            /** 仁德插入健保卡後，唯一的門診預約 */
            val RENDE_DIVISION_ONLY = ScheduleBean(
                doctor = "e666",
                division = "399"
            )

            /** 仁德疫苗施打 */
            val RENDE_VACCINE = ScheduleBean(
                doctor = "e666",
                division = "398"
            )

            /** 仁德體檢 */
            val RENDE_CHECK_UP = ScheduleBean(
                doctor = "e666",
                division = "400"
            )
        }
    }
}
