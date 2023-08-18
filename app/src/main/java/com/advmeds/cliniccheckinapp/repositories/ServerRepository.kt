package com.advmeds.cliniccheckinapp.repositories

import com.advmeds.cliniccheckinapp.models.remote.mScheduler.ApiService
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest

class ServerRepository(private val service: ApiService) {

    /** @see ApiService.getClinicGuardian */
    suspend fun getClinicGuardian(
        clinicId: String,
    ) = service.getClinicGuardian(clinicId)

    /** @see ApiService.getPatients */
    suspend fun getPatients(
        clinicId: String,
        nationalId: String,
        doctors: Array<String>,
        rooms: Array<Int>
    ) = service.getPatients(clinicId, nationalId, doctors, rooms)

    /** @see ApiService.getSchedules */
    suspend fun getSchedules(
        clinicId: String,
    ) = service.getSchedules(clinicId)

    /** @see ApiService.createAppointment */
    suspend fun createsAppointment(
        request: CreateAppointmentRequest
    ) = service.createAppointment(request)

    suspend fun checkControllerAppVersion(
        version: String,
        name: String
    ) = service.checkControllerAppVersion(version, name)
}