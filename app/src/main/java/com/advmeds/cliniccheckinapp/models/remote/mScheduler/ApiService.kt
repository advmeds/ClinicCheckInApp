package com.advmeds.cliniccheckinapp.models.remote.mScheduler

import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    /**
     * Get the clinic setting，such as logo, language
     * @param clinicId clinic identity id
     * @return clinic setting
     */
    @GET("api/v1/clinics/{id}/guardians")
    suspend fun getClinicGuardian(
        @Path("id")
        clinicId: String
    ): Response<GetClinicGuardianResponse>

    /**
     * find the patient's appointment according to the national id or chart no.
     *
     * @param clinicId clinic identity id
     * @param nationalId national id or chart no
     * @param doctors specific doctor id, only find appointment for specific doctor
     * @param rooms specific room id, only find appointment for specific room
     * @return patient's appointment
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
     * get division that can be registered now.
     *
     * @param clinicId clinic identity id
     * @return division
     */
    @GET("api/v1/clinics/get_schedule")
    suspend fun getSchedules(
        @Query("clinic_id")
        clinicId: String,
    ): Response<GetScheduleResponse>

    /**
     * make an appointment
     *
     * @param request
     * @return success or failure
     */
    @POST("api/v1/clinics/creates_appointment")
    suspend fun createAppointment(
        @Body
        request: CreateAppointmentRequest
    ): Response<CreateAppointmentResponse>

    /**
     * make an appointment
     *
     * @param version
     * @param name - name of the app
     * @return link to apk and number next version or message about no new version
     */
    @GET("api/v1/controller")
    suspend fun checkControllerAppVersion(
        @Query("version") version: String,
        @Query("name") name: String
    ): Response<ControllerAppVersionResponse>

}