package com.advmeds.cliniccheckinapp.models.remote.mScheduler

import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetPatientsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    /**
     * 帶入身分證或病歷號查詢病患今天預約掛號資訊。
     *
     * @param clinicId 診所代碼
     * @param nationalId 身分證或病歷號
     * @return 病患今天預約掛號資訊
     */
    @GET("api/v1/clinics/get_patients")
    suspend fun getPatients(
        @Query("clinic_id")
        clinicId: String,

        @Query("patient")
        nationalId: String
    ): Response<GetPatientsResponse>
}