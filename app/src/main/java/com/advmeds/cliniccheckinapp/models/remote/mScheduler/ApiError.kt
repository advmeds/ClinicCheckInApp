package com.advmeds.cliniccheckinapp.models.remote.mScheduler

import com.advmeds.cliniccheckinapp.R

enum class ApiError(val rawValue: Int) {
    PARAMS_NULL(10001),
    CLINIC_NOT_FOUND(10002),
    DOCTOR_NOT_FOUND(10003),
    DIVISION_NOT_FOUND(10004),
    PATIENT_NAME_NULL(10005),
    PATIENT_MOBILE_NULL(10006),
    PATIENT_BIRTH_NULL(10007),
    PATIENT_NATIONAL_ID_NULL(10008),
    DATETIME_WRONG(10009);

    companion object {
        fun initWith(rawValue: Int) = values().find { it.rawValue == rawValue }
    }

    val resStringID: Int
        get() = when(this) {
            PARAMS_NULL -> R.string.mScheduler_api_error_10001
            CLINIC_NOT_FOUND -> R.string.mScheduler_api_error_10002
            DOCTOR_NOT_FOUND -> R.string.mScheduler_api_error_10003
            DIVISION_NOT_FOUND -> R.string.mScheduler_api_error_10004
            PATIENT_NAME_NULL -> R.string.mScheduler_api_error_10005
            PATIENT_MOBILE_NULL -> R.string.mScheduler_api_error_10006
            PATIENT_BIRTH_NULL -> R.string.mScheduler_api_error_10007
            PATIENT_NATIONAL_ID_NULL -> R.string.mScheduler_api_error_10008
            DATETIME_WRONG -> R.string.mScheduler_api_error_10009
        }
}