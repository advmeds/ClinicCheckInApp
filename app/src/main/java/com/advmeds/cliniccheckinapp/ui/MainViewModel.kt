package com.advmeds.cliniccheckinapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.advmeds.cliniccheckinapp.BuildConfig
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.ApiService
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.CreateAppointmentResponse
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetClinicGuardianResponse
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetPatientsResponse
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetScheduleResponse
import com.advmeds.cliniccheckinapp.repositories.ServerRepository
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo
import com.advmeds.cliniccheckinapp.utils.isNationId
import com.advmeds.cliniccheckinapp.utils.isPTCHCaseId
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okio.Buffer
import retrofit2.Retrofit
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferencesRepo = SharedPreferencesRepo.getInstance(getApplication())

    /** @see SharedPreferencesRepo.babySerialNo */
    var babySerialNo: Int
        get() = sharedPreferencesRepo.babySerialNo
        set(value) {
            sharedPreferencesRepo.babySerialNo = value
        }

    private val format = Json {
        isLenient = true
        coerceInputValues = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor {
            val request = it.request()

            val buffer = Buffer()
            request.body()?.writeTo(buffer)

            Timber.d("URL: ${request.url()}")
            Timber.d("Method: ${request.method()}")
            Timber.d("Request: ${buffer.readUtf8()}")

            return@addInterceptor it.proceed(request)
        }
        .build()

    @OptIn(ExperimentalSerializationApi::class)
    private val serverRepo: ServerRepository
        get() {
            val retrofit = Retrofit.Builder()
                .client(client)
                .baseUrl(sharedPreferencesRepo.mSchedulerServerDomain)
                .addConverterFactory(format.asConverterFactory(MediaType.get("application/json")))
                .build()

            return ServerRepository(
                retrofit.create(ApiService::class.java)
            )
        }

    val getGuardianStatus = MutableLiveData<GetGuardianStatus>()
    val checkInStatus = MutableLiveData<CheckInStatus>()
    val getSchedulesStatus = MutableLiveData<GetSchedulesStatus>()
    val createAppointmentStatus = MutableLiveData<CreateAppointmentStatus>()

    private var getGuardianJob: Job? = null
    private var checkJob: Job? = null
    private var getSchedulesJob: Job? = null
    private var createAppointmentJob: Job? = null

    val clinicGuardian = MutableLiveData<GetClinicGuardianResponse?>()
    private var patient: CreateAppointmentRequest.Patient? = null

    fun getClinicGuardian(completion: ((GetClinicGuardianResponse) -> Unit)? = null) {
        createAppointmentJob?.cancel()
        getSchedulesJob?.cancel()
        checkJob?.cancel()
        getGuardianJob?.cancel()

        getGuardianJob = viewModelScope.launch {
            getGuardianStatus.value = GetGuardianStatus.Checking

            val response = try {
                val connectivityManager =
                    getApplication<MainApplication>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                while (connectivityManager.activeNetworkInfo?.isConnected != true) {
                    withContext(Dispatchers.IO) { delay(1000) }
                }

                val result = serverRepo.getClinicGuardian(sharedPreferencesRepo.orgId)

                Timber.d("Status code: ${result.code()}")
                Timber.d("Response: ${format.encodeToString(result.body())}")

                if (result.isSuccessful) {
                    result.body()!!.also {
                        sharedPreferencesRepo.logoUrl = it.logo
                    }
                } else {
                    GetClinicGuardianResponse(
                        success = false,
                        code = result.code(),
                        message = result.message()
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)

                GetClinicGuardianResponse(
                    success = false,
                    code = 0,
                    message = when (e) {
                        is UnknownHostException -> {
                            getApplication<MainApplication>().getString(
                                R.string.no_internet
                            )
                        }
                        is SocketTimeoutException -> {
                            getApplication<MainApplication>().getString(
                                R.string.service_timeout
                            )
                        }
                        else -> {
                            e.localizedMessage
                        }
                    }
                )
            }

            clinicGuardian.value = response.takeIf { it.success }

            completion?.let { it(response) }

            getGuardianStatus.value = GetGuardianStatus.NotChecking(response)
        }

        getGuardianJob?.invokeOnCompletion {
            it ?: return@invokeOnCompletion

            Timber.e(it)

            clinicGuardian.value = null

            getGuardianStatus.value = if (it !is CancellationException) {
                GetGuardianStatus.Fail(it)
            } else {
                GetGuardianStatus.NotChecking(null)
            }
        }
    }

    fun getPatients(
        patient: CreateAppointmentRequest.Patient,
        completion: ((GetPatientsResponse) -> Unit)? = null
    ) {
        if (getGuardianJob?.isActive == true) return

        createAppointmentJob?.cancel()
        getSchedulesJob?.cancel()
        checkJob?.cancel()

        this.patient = patient

        checkJob = viewModelScope.launch {
            val application = getApplication<MainApplication>()

            if (clinicGuardian.value == null) {
                checkInStatus.value = CheckInStatus.NotChecking(
                    response = GetPatientsResponse(
                        success = false,
                        code = 0,
                        message = application.getString(R.string.clinic_data_not_found)
                    )
                )
                return@launch
            }

            if (patient.nationalId.isBlank()) {
                checkInStatus.value = CheckInStatus.NotChecking(
                    response = GetPatientsResponse(
                        success = false,
                        code = 0,
                        message = String.format(
                            application.getString(R.string.national_id_input_hint),
                            application.getString(R.string.national_id)
                        )
                    )
                )
                return@launch
            }

            if (!(patient.nationalId.isNationId || (BuildConfig.BUILD_TYPE == "ptch" && patient.nationalId.isPTCHCaseId))) {
                checkInStatus.value = CheckInStatus.NotChecking(
                    response = GetPatientsResponse(
                        success = false,
                        code = 0,
                        message = String.format(
                            application.getString(R.string.national_id_format_invalid),
                            application.getString(R.string.national_id)
                        )
                    )
                )
                return@launch
            }

            checkInStatus.value = CheckInStatus.Checking

            val response = try {
                val result = serverRepo.getPatients(sharedPreferencesRepo.orgId, patient.nationalId)

                Timber.d("Status code: ${result.code()}")
                Timber.d("Response: ${format.encodeToString(result.body())}")

                if (result.isSuccessful) {
                    result.body()!!
                } else {
                    GetPatientsResponse(
                        success = false,
                        code = result.code(),
                        message = result.message()
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)

                GetPatientsResponse(
                    success = false,
                    code = 0,
                    message = when (e) {
                        is UnknownHostException -> {
                            getApplication<MainApplication>().getString(
                                R.string.no_internet
                            )
                        }
                        is SocketTimeoutException -> {
                            getApplication<MainApplication>().getString(
                                R.string.service_timeout
                            )
                        }
                        else -> {
                            e.localizedMessage
                        }
                    }
                )
            }

            completion?.let { it(response) }

            checkInStatus.value = CheckInStatus.NotChecking(response)
        }

        checkJob?.invokeOnCompletion {
            it ?: return@invokeOnCompletion

            Timber.e(it)

            checkInStatus.value = if (it !is CancellationException) {
                CheckInStatus.Fail(it)
            } else {
                CheckInStatus.NotChecking(null)
            }
        }
    }

    fun getSchedule(completion: ((GetScheduleResponse) -> Unit)? = null) {
        if (checkJob?.isActive == true) return

        createAppointmentJob?.cancel()
        getSchedulesJob?.cancel()

        getSchedulesJob = viewModelScope.launch {
            getSchedulesStatus.value = GetSchedulesStatus.Checking

            val response = try {
                val result = serverRepo.getSchedules(sharedPreferencesRepo.orgId)

                Timber.d("Status code: ${result.code()}")
                Timber.d("Response: ${format.encodeToString(result.body())}")

                if (result.isSuccessful) {
                    result.body()!!
                } else {
                    GetScheduleResponse(
                        success = false,
                        code = result.code(),
                        message = result.message()
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)

                GetScheduleResponse(
                    success = false,
                    code = 0,
                    message = when (e) {
                        is UnknownHostException -> {
                            getApplication<MainApplication>().getString(
                                R.string.no_internet
                            )
                        }
                        is SocketTimeoutException -> {
                            getApplication<MainApplication>().getString(
                                R.string.service_timeout
                            )
                        }
                        else -> {
                            e.localizedMessage
                        }
                    }
                )
            }

            completion?.let { it(response) }

            getSchedulesStatus.value = GetSchedulesStatus.NotChecking(response)
        }

        getSchedulesJob?.invokeOnCompletion {

            it ?: return@invokeOnCompletion

            Timber.e(it)

            getSchedulesStatus.value = if (it !is CancellationException) {
                GetSchedulesStatus.Fail(it)
            } else {
                GetSchedulesStatus.NotChecking(null)
            }
        }
    }

    fun createAppointment(
        schedule: GetScheduleResponse.ScheduleBean,
        patient: CreateAppointmentRequest.Patient? = null,
        completion: ((CreateAppointmentResponse) -> Unit)? = null
    ) {
        if (getSchedulesJob?.isActive == true) return

        createAppointmentJob?.cancel()

        createAppointmentJob = viewModelScope.launch {
            createAppointmentStatus.value = CreateAppointmentStatus.Checking

            val response = try {
                val request = CreateAppointmentRequest(
                    clinicId = sharedPreferencesRepo.orgId,
                    doctor = schedule.doctor,
                    division = schedule.division,
                    startsAt = schedule.startsAt,
                    endsAt = schedule.endsAt,
                    patient = requireNotNull(patient ?: this@MainViewModel.patient) {
                        getApplication<MainApplication>().getString(R.string.mScheduler_api_error_10008)
                    }
                )

                val result = serverRepo.createsAppointment(request)

                Timber.d("Status code: ${result.code()}")
                Timber.d("Response: ${format.encodeToString(result.body())}")

                if (result.isSuccessful) {
                    val response = result.body()!!

                    if (!response.success &&
                        response.code == 10014 &&
                        schedule.doctor == "CA" &&
                        schedule.division == "0000"
                    ) {
                        response.copy(
                            message = getApplication<MainApplication>().getString(R.string.mScheduler_api_error_10014_ptch_ca)
                        )
                    } else {
                        response
                    }
                } else {
                    CreateAppointmentResponse(
                        success = false,
                        code = result.code(),
                        message = result.message()
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)

                CreateAppointmentResponse(
                    success = false,
                    code = 0,
                    message = when (e) {
                        is UnknownHostException -> {
                            getApplication<MainApplication>().getString(
                                R.string.no_internet
                            )
                        }
                        is SocketTimeoutException -> {
                            getApplication<MainApplication>().getString(
                                R.string.service_timeout
                            )
                        }
                        else -> {
                            e.localizedMessage
                        }
                    }
                )
            }

            completion?.let { it(response) }

            createAppointmentStatus.value = CreateAppointmentStatus.NotChecking(response)
        }

        createAppointmentJob?.invokeOnCompletion {

            it ?: return@invokeOnCompletion

            Timber.e(it)

            createAppointmentStatus.value = if (it !is CancellationException) {
                CreateAppointmentStatus.Fail(it)
            } else {
                CreateAppointmentStatus.NotChecking(null)
            }
        }
    }

    fun cancelJobOnCardAbsent() {
        createAppointmentJob?.cancel()
        getSchedulesJob?.cancel()
        checkJob?.cancel()
    }

    sealed class GetGuardianStatus {
        object Checking : GetGuardianStatus()
        data class NotChecking(val response: GetClinicGuardianResponse?) : GetGuardianStatus()
        data class Fail(val throwable: Throwable) : GetGuardianStatus()
    }

    sealed class CheckInStatus {
        object Checking : CheckInStatus()
        data class NotChecking(val response: GetPatientsResponse?) : CheckInStatus()
        data class Fail(val throwable: Throwable) : CheckInStatus()
    }

    sealed class GetSchedulesStatus {
        object Checking : GetSchedulesStatus()
        data class NotChecking(val response: GetScheduleResponse?) : GetSchedulesStatus()
        data class Fail(val throwable: Throwable) : GetSchedulesStatus()
    }

    sealed class CreateAppointmentStatus {
        object Checking : CreateAppointmentStatus()
        data class NotChecking(val response: CreateAppointmentResponse?) : CreateAppointmentStatus()
        data class Fail(val throwable: Throwable) : CreateAppointmentStatus()
    }
}