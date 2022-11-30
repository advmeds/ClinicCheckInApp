package com.advmeds.cliniccheckinapp.ui

import android.app.Application
import android.os.Build
import android.text.Html
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.dialog.ErrorDialogFragment
import com.advmeds.cliniccheckinapp.dialog.SuccessDialogFragment
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.ApiError
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.ApiService
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.CreateAppointmentResponse
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetPatientsResponse
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetScheduleResponse
import com.advmeds.cliniccheckinapp.repositories.ServerRepository
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo
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

    private val sharedPreferencesRepo = SharedPreferencesRepo.getInstance(getApplication())

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

    val checkInStatus = MutableLiveData<CheckInStatus>()
    val getSchedulesStatus = MutableLiveData<GetSchedulesStatus>()
    val createAppointmentStatus = MutableLiveData<CreateAppointmentStatus>()

    private var checkJob: Job? = null
    private var getSchedulesJob: Job? = null
    private var createAppointmentJob: Job? = null

    private var patient: CreateAppointmentRequest.Patient? = null

    fun getPatients(patient: CreateAppointmentRequest.Patient, completion: ((GetPatientsResponse) -> Unit)? = null) {
        createAppointmentJob?.cancel()
        getSchedulesJob?.cancel()
        checkJob?.cancel()

        this.patient = patient

        checkJob = viewModelScope.launch {
            checkInStatus.value = CheckInStatus.Checking

            val response = try {
                val result = serverRepo.getPatients(sharedPreferencesRepo.orgId, patient.nationalId)

                Timber.d("Status code: ${result.code()}")
                Timber.d("Response: ${format.encodeToString(result.body())}")

                result.body()!!
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
                    },
                    patients = emptyList()
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
        getSchedulesJob?.cancel()

        getSchedulesJob = viewModelScope.launch {
            getSchedulesStatus.value = GetSchedulesStatus.Checking

            val response = try {
                val result = serverRepo.getSchedules(sharedPreferencesRepo.orgId)

                Timber.d("Status code: ${result.code()}")
                Timber.d("Response: ${format.encodeToString(result.body())}")

                result.body()!!
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
                    },
                    schedules = emptyList()
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
        doctor: String,
        division: String,
        startsAt: String,
        endsAt: String,
        completion: ((CreateAppointmentResponse) -> Unit)? = null
    ) {
        createAppointmentJob?.cancel()

        createAppointmentJob = viewModelScope.launch {
            createAppointmentStatus.value = CreateAppointmentStatus.Checking

            val response = try {
                val request = CreateAppointmentRequest(
                    clinicId = sharedPreferencesRepo.orgId,
                    doctor = doctor,
                    division = division,
                    startsAt = startsAt,
                    endsAt = endsAt,
                    patient = patient ?: CreateAppointmentRequest.Patient()
                )

                val result = serverRepo.createsAppointment(request)

                Timber.d("Status code: ${result.code()}")
                Timber.d("Response: ${format.encodeToString(result.body())}")

                result.body()!!
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
}