package com.advmeds.cliniccheckinapp.models.remote.mScheduler.request

import com.advmeds.cliniccheckinapp.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateAppointmentRequest(
    /** clinic identity id */
    @SerialName("clinic_id")
    val clinicId: String = "",

    /** doctor id */
    val doctor: String = "",

    /** division id */
    val division: String = "",

    /** patient information */
    val patient: Patient = Patient()
) {
    @Serializable
    data class Patient(
        /** patient name */
        val name: String = "",

        /** patient date of birth */
        val birthday: String = "",

        /** patient mobile */
        val mobile: String = "",

        /** patient national id */
        @SerialName("national_id")
        val nationalId: String = "",
    )

    /** patient national id pattern */
    enum class NationalIdFormat(private val pattern: String) {
        /** national id (default) */
        DEFAULT("^[A-Z][A-Z\\d]\\d{8}\$"),

        /** chart no */
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
