package com.advmeds.cliniccheckinapp.repositories

import com.advmeds.cliniccheckinapp.models.remote.mScheduler.ApiService
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetPatientsResponse
import retrofit2.Response

class ServerRepository(private val service: ApiService) {

    /** @see ApiService.getPatients */
    suspend fun getPatients(
        clinicId: String,
        nationalId: String
    ): Response<GetPatientsResponse> = service.getPatients(clinicId, nationalId)
}