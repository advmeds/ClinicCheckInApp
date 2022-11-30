package com.advmeds.cliniccheckinapp.repositories

import com.advmeds.cliniccheckinapp.models.remote.mScheduler.ApiService
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest

class ServerRepository(private val service: ApiService) {

    /** @see ApiService.getPatients */
    suspend fun getPatients(
        clinicId: String,
        nationalId: String
    ) = service.getPatients(clinicId, nationalId)

    /** @see ApiService.getSchedules */
    suspend fun getSchedules(
        clinicId: String,
    ) = service.getSchedules(clinicId)

    /** @see ApiService.createAppointment */
    suspend fun createsAppointment(
        request: CreateAppointmentRequest
    ) = service.createAppointment(request)
}