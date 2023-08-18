package com.advmeds.cliniccheckinapp.models.remote.mScheduler.response

import com.advmeds.cliniccheckinapp.utils.NativeText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPatientsResponse(
    /** is successful */
    val success: Boolean = false,

    /** error code */
    val code: Int = 0,

    /** error message */
    val message: String = "",
    val _message: NativeText = NativeText.Simple(""),

    /** patient's appointment */
    val patients: List<PatientBean> = emptyList()
) {
    @Serializable
    data class PatientBean(
        /** doctor name */
        val doctor: String = "",

        /** division name */
        val division: String = "",

        /** national id */
        @SerialName("national_id")
        val nationalId: String = "",

        /** chart no */
        @SerialName("patient_id")
        val patientId: String = "",

        /** patient name */
        val name: String = "",

        /** patient date of birth */
        val birthday: String = "",

        /** note */
        val note: String = "",

        /** queue number */
        @SerialName("serial_num")
        val serialNo: Int = 0
    )
}