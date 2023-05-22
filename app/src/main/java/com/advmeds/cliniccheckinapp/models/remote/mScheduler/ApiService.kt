package com.advmeds.cliniccheckinapp.models.remote.mScheduler

import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.CreateAppointmentResponse
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetClinicGuardianResponse
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetPatientsResponse
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetScheduleResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    /**
     * 取得該診間的相關設定，例如LOGO、語系
     * @param clinicId 診所代碼
     */
    @GET("api/v1/clinics/{id}/guardians")
    suspend fun getClinicGuardian(
        @Path("id")
        clinicId: String
    ): Response<GetClinicGuardianResponse>

    /**
     * 帶入身分證或病歷號查詢病患今天預約掛號資訊。
     *
     * @param clinicId 診所代碼
     * @param nationalId 身分證或病歷號
     * @param rooms 診間陣列
     * @return 病患今天預約掛號資訊
     */
    @GET("api/v1/clinics/get_patients")
    suspend fun getPatients(
        @Query("clinic_id")
        clinicId: String,

        @Query("patient")
        nationalId: String,

        @Query("doctors[]")
        doctors: Array<String>,

        @Query("rooms[]")
        rooms: Array<Int>
    ): Response<GetPatientsResponse>

    /**
     * 取得現在可以掛號的門診。
     *
     * @param clinicId 診所代碼
     * @return 現在可以掛號的門診資訊
     */
    @GET("api/v1/clinics/get_schedule")
    suspend fun getSchedules(
        @Query("clinic_id")
        clinicId: String,
    ): Response<GetScheduleResponse>

    /**
     * 建立預約
     *
     * @param request 建立預約需要的請求
     * @return 成功與否
     */
    @POST("api/v1/clinics/creates_appointment")
    suspend fun createAppointment(
        @Body
        request: CreateAppointmentRequest
    ): Response<CreateAppointmentResponse>
}