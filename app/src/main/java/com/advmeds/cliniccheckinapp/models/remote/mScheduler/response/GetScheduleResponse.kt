package com.advmeds.cliniccheckinapp.models.remote.mScheduler.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class GetScheduleResponse(
    /** is successful */
    val success: Boolean = false,

    /** error code */
    val code: Int = 0,

    /** error message */
    val message: String = "",

    /** division information */
    val schedules: List<ScheduleBean> = emptyList()
) {
    @Serializable
    data class ScheduleBean(
        /** doctor id */
        val doctor: String = "",

        /** doctor name */
        @SerialName("doctor_name")
        val doctorName: String = "",

        /** doctor title */
        @SerialName("doctor_title")
        val doctorTitle: String = "",

        /** doctor alias */
        @SerialName("doctor_alias")
        val doctorAlias: String = "",

        /** doctor photo */
        @SerialName("doctor_photo")
        val doctorPhoto: String = "",

        /** doctor experience */
        @SerialName("doctor_experience")
        val doctorExperience: String = "",

        /** doctor specialization */
        @SerialName("doctor_specialization")
        val doctorSpecialization: String = "",

        /** division id */
        val division: String = "",

        /** division name */
        @SerialName("division_name")
        val divisionName: String = "",

        /** quota of appointment */
        @SerialName("patient_quota")
        val patientQuota: Int = 0,

        /** division start time */
        @SerialName("starts_at")
        val startsAt: String = "",

        /** division end time */
        @SerialName("ends_at")
        val endsAt: String = "",

        /** waiting num */
        @SerialName("waiting_num")
        val waitingNum: Int = 0,

        /** is remote */
        @SerialName("is_remote")
        val isRemote: Boolean = false,

        /** status */
        val status: Int = 1,
    ) {
        companion object {
            /** Taiwan PTCH specify Division，小兒心超 */
            val PTCH_BABY = ScheduleBean(
                doctor = "CA",
                division = "0000"
            )

            /** Taiwan RenDe Clinic specify Division，門診預約 */
            val RENDE_DIVISION_ONLY = ScheduleBean(
                doctor = "e666",
                division = "399"
            )

            /** Taiwan RenDe Clinic specify Division，疫苗施打 */
            val RENDE_VACCINE = ScheduleBean(
                doctor = "e666",
                division = "398"
            )

            /** Taiwan RenDe Clinic specify Division，體檢 */
            val RENDE_CHECK_UP = ScheduleBean(
                doctor = "e666",
                division = "400"
            )
        }
    }
}
