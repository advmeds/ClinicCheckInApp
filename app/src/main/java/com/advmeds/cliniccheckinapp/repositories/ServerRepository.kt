package com.advmeds.cliniccheckinapp.repositories

import com.advmeds.cliniccheckinapp.models.remote.mScheduler.ApiService
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentShortRequest

class ServerRepository(private val service: ApiService) {

    /** @see ApiService.getClinicGuardian */
    suspend fun getClinicGuardian(
        clinicId: String,
    ) = service.getClinicGuardian(clinicId)

    /** @see ApiService.getPatients */
    suspend fun getPatients(
        clinicId: String,
        nationalId: String,
        rooms: List<String>,
        doctors: List<String>
    ) = service.getPatients(
        clinicId = clinicId,
        nationalId = nationalId,
        rooms = rooms,
        doctors = doctors
    )

    /** @see ApiService.getSchedules */
    suspend fun getSchedules(
        clinicId: String,
    ) = service.getSchedules(clinicId)

    /** @see ApiService.createAppointment */
    suspend fun createsAppointment(
        request: CreateAppointmentRequest
    ) = service.createAppointment(request)

    suspend fun createsAppointment(
        request: CreateAppointmentShortRequest
    ) = service.createAppointment(request)
}