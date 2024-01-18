package com.advmeds.cliniccheckinapp.repositories

import com.advmeds.cliniccheckinapp.BuildConfig
import com.advmeds.cliniccheckinapp.models.events.EventRepository
import com.advmeds.cliniccheckinapp.models.events.entities.EventData
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateActionLogRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.SessionRequest
import com.advmeds.cliniccheckinapp.repositories.AnalyticsRepository.Companion.getCurrentDateTime
import java.text.SimpleDateFormat
import java.util.*

interface AnalyticsRepository {

    fun setServerRepository(serverRepository: ServerRepository)

    suspend fun sendEvent(
        eventName: String,
        params: MutableMap<String, Any>? = null,
        sessionNumber: Long? = null,
        destination: DestinationType = DestinationType.LOCAL
    )

    companion object {
        const val SOURCE_ACTION = "source_value"
        const val SOURCE_SCREEN = "SourceScreen"
        const val APP_VERSION_NAME = "app_version_name"
        const val APP_VERSION_CODE = "app_version_code"
        const val TIME = "log_time"

        fun getCurrentDateTime(): String {
            val currentDateAndTime = Date()

            val pattern = "yyyy-MM-dd'T'HH:mm:ss"
            val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())

            return simpleDateFormat.format(currentDateAndTime)
        }
    }

    enum class DestinationType {
        LOCAL,
        SERVER,
        LOCAL_TO_SERVER
    }
}

class AnalyticsRepositoryImpl private constructor(
    private val eventRepository: EventRepository,
    private val sharedPreferencesRepo: SharedPreferencesRepo,
) : AnalyticsRepository {

    private var serverRepository: ServerRepository? = null

    override fun setServerRepository(serverRepository: ServerRepository) {
        this.serverRepository = serverRepository
    }

    companion object {
        @Volatile
        private var INSTANCE: AnalyticsRepositoryImpl? = null

        fun getInstance(
            eventRepository: EventRepository,
            sharedPreferencesRepo: SharedPreferencesRepo
        ): AnalyticsRepository {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = AnalyticsRepositoryImpl(eventRepository, sharedPreferencesRepo)
                }
                return INSTANCE!!
            }
        }
    }

    override suspend fun sendEvent(
        eventName: String,
        params: MutableMap<String, Any>?,
        sessionNumber: Long?,
        destination: AnalyticsRepository.DestinationType
    ) {
        if (sessionNumber == null) {
            setGlobalProperties(params)
        }

        when (destination) {
            AnalyticsRepository.DestinationType.LOCAL -> {
                logEventToLocal(eventName, params, sessionNumber)
            }
            AnalyticsRepository.DestinationType.LOCAL_TO_SERVER -> {
                logEventFromLocalToServer()
            }
            AnalyticsRepository.DestinationType.SERVER -> TODO()
        }
    }

    private suspend fun logEventToLocal(
        eventName: String,
        params: MutableMap<String, Any>?,
        sessionNumber: Long?
    ) {
        params?.let {

            val sessionId = eventRepository.getOrCreateNewSession(
                sessionNumber = sessionNumber ?: sharedPreferencesRepo.sessionNumber.toLong(),
                deviceId = sharedPreferencesRepo.deviceId
            )

            val eventData = EventData(eventName = eventName, params = params, sessionId = sessionId)
            try {
                eventRepository.saveEventInDataBase(eventData)
            } catch (_: java.lang.Exception) { }
        }
    }

    private suspend fun logEventFromLocalToServer() {
        eventRepository.deleteSessionThatHaveBeenSentOnServer()

        serverRepository?.let {
            try {
                val sessionMap = mutableMapOf<Long, List<EventData>>()

                val sessionsForSend =
                    eventRepository.getAllSessionsThatHaveNotSentOnServerExceptCurrent(
                        sharedPreferencesRepo.sessionNumber.toLong()
                    ) ?: return

                sessionsForSend.forEach { session ->
                    val eventData =
                        eventRepository.getEventBySessionId(session.id) ?: return
                    sessionMap[session.sessionNumber] = eventData
                }

                sessionMap.values.forEach { session ->
                    session.forEach { eventDataDb ->
                        val param = eventRepository.getParamById(eventDataDb.id)
                        eventDataDb.params.putAll(param)
                    }
                }

                val request = CreateActionLogRequest(
                    clinicId = sharedPreferencesRepo.orgId.toInt(),
                    input = sharedPreferencesRepo.doctors.joinToString(","),
                    content = SessionRequest.fromMapToSessionRequest(sessionsForSend, sessionMap)
                )

                if (request.content.isEmpty()) {
                    return
                }

                val result = it.sendActionLog(request)

                if (result.isSuccessful) {
                    sessionsForSend.forEach { session ->
                        eventRepository.markSessionThatHaveBeenSentOnServer(
                            sessionId = session.id,
                            wasSendOnServer = true
                        )
                    }
                } else {
                    sendEvent(
                        eventName = "send local logs to server with complete with error",
                        params = mutableMapOf(
                            "error code" to result.code(),
                            "error message" to result.message()
                        )
                    )
                }
            } catch (e: Exception) {
                sendEvent(
                    eventName = "send local logs to server with complete with error",
                    params = mutableMapOf("error" to e)
                )
            } finally {
                serverRepository = null
            }
        }
    }

    private fun setGlobalProperties(params: MutableMap<String, Any>?) {
        params?.let {
            params[AnalyticsRepository.APP_VERSION_NAME] =
                BuildConfig.VERSION_NAME
            params[AnalyticsRepository.APP_VERSION_CODE] =
                BuildConfig.VERSION_CODE
            params[AnalyticsRepository.TIME] =
                getCurrentDateTime()
        }
    }
}