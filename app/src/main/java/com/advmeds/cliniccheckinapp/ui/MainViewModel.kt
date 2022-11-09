package com.advmeds.cliniccheckinapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.ApiService
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetPatientsResponse
import com.advmeds.cliniccheckinapp.repositories.ServerRepository
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.*
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

        data class NotChecking(val response: GetPatientsResponse) : CheckInStatus()

        data class Fail(val throwable: Throwable) : CheckInStatus()
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

    val checkInStatus = MutableLiveData<CheckInStatus>()

    private var checkJob: Job? = null

    fun getPatients(nationalId: String, checkInCompletion: (() -> Unit)? = null) {
        checkJob?.cancel()

        checkJob = viewModelScope.launch {
            checkInStatus.value = CheckInStatus.Checking

            val retrofit = Retrofit.Builder()
                .client(client)
                .baseUrl(sharedPreferencesRepo.mSchedulerServerDomain)
                .addConverterFactory(format.asConverterFactory(MediaType.get("application/json")))
                .build()

            val serverRepo = ServerRepository(
                retrofit.create(ApiService::class.java)
            )

            val response = try {
                val result = serverRepo.getPatients(sharedPreferencesRepo.orgId, nationalId)

                Timber.d("Status code: ${result.code()}")
                Timber.d("Response: ${format.encodeToString(result.body())}")

                if (result.isSuccessful) {
                    result.body()!!
                } else {
                    GetPatientsResponse(
                        success = false,
                        code = result.code(),
                        message = "Status code: ${result.code()}",
                        patients = emptyList()
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
                    },
                    patients = emptyList()
                )
            }

            if (response.success) {
                checkInCompletion?.let { it() }
            }

            checkInStatus.value = CheckInStatus.NotChecking(response)
        }

        checkJob?.invokeOnCompletion {

            it ?: return@invokeOnCompletion

            Timber.e(it)

            checkInStatus.value = CheckInStatus.Fail(it)
        }
    }
}