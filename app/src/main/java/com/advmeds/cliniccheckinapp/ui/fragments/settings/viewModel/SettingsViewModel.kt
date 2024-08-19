package com.advmeds.cliniccheckinapp.ui.fragments.settings.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.advmeds.cliniccheckinapp.BuildConfig
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.dialog.EditCheckInItemDialog
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.ApiService
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.LogInterceptor
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.ControllerAppVersionResponse
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.AutomaticAppointmentSettingModel
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueueingMachineSettingModel
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueuingBoardSettingModel
import com.advmeds.cliniccheckinapp.repositories.DownloadControllerRepository
import com.advmeds.cliniccheckinapp.repositories.ServerRepository
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo
import com.advmeds.cliniccheckinapp.ui.fragments.settings.eventLogger.SettingsEventLogger
import com.advmeds.cliniccheckinapp.utils.DownloadController
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okio.Buffer
import retrofit2.Response
import retrofit2.Retrofit
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SettingsViewModel(
    application: Application,
    private val downloadControllerRepository: DownloadControllerRepository,
    private val settingsEventLogger: SettingsEventLogger
) : AndroidViewModel(application) {
    private val sharedPreferencesRepo = SharedPreferencesRepo.getInstance(getApplication())


    /** Settings ui state */
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()


    private var updateJob: Job? = null

    fun checkForUpdates() {

        updateJob?.cancel()

        updateJob = viewModelScope.launch {
            val versionName = BuildConfig.VERSION_NAME
            val updateName = BuildConfig.UPDATE_NAME

            try {
                val response = serverRepo.checkControllerAppVersion(
                    name = updateName,
                    version = versionName
                )

                if (response.isSuccessful) {
                    emitSuccessCheckControllerAppVersion(response)
                } else {
                    _uiState.update { currentState ->
                        currentState.copy(
                            updateSoftwareRequestStatus = UpdateSoftwareRequestStatus.FAIL,
                            updateSoftwareDownloadingStatus = UpdateSoftwareDownloadingStatus.FAIL,
                            updateSoftwareDialogText = R.string.unexpected_error
                        )
                    }
                }

                responseCheckForUpdate(response)
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        updateSoftwareRequestStatus = UpdateSoftwareRequestStatus.FAIL,
                        updateSoftwareDownloadingStatus = UpdateSoftwareDownloadingStatus.FAIL,
                        updateSoftwareDialogText = R.string.unexpected_error
                    )
                }
            }
        }
    }

    private fun emitSuccessCheckControllerAppVersion(
        response: Response<ControllerAppVersionResponse>
    ) {
        if (response.body()!!.success) {
            if (response.body()!!.isAvailable) {
                _uiState.update { currentState ->
                    currentState.copy(
                        updateSoftwareNotificationUrl = response.body()!!.url,
                        updateSoftwareNotificationVersion = response.body()!!.version
                    )
                }
                startDownLoading(url = response.body()!!.url, version = response.body()!!.version)
                appEventDownloadUpdate("start download update")
            } else {
                _uiState.update { currentState ->
                    currentState.copy(
                        updateSoftwareRequestStatus = UpdateSoftwareRequestStatus.SUCCESS,
                        updateSoftwareDownloadingStatus = UpdateSoftwareDownloadingStatus.SUCCESS,
                        updateSoftwareDialogText = R.string.update_software_dialog_update_is_not_available
                    )
                }
            }
        } else {
            if (!response.body()!!.isAvailable) {
                _uiState.update { currentState ->
                    currentState.copy(
                        updateSoftwareRequestStatus = UpdateSoftwareRequestStatus.SUCCESS,
                        updateSoftwareDownloadingStatus = UpdateSoftwareDownloadingStatus.SUCCESS,
                        updateSoftwareDialogText = R.string.update_software_dialog_update_is_not_available
                    )
                }
            } else if (response.body() == null) {
                _uiState.update { currentState ->
                    currentState.copy(
                        updateSoftwareRequestStatus = UpdateSoftwareRequestStatus.FAIL,
                        updateSoftwareDialogText = R.string.unexpected_error
                    )
                }
            }
        }
    }


    private fun startDownLoading(url: String, version: String) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadControllerRepository.startProcessOfDownload(
                url = url,
                version = version
            )
        }

        viewModelScope.launch(Dispatchers.Main) {

            downloadControllerRepository.getProgressOfDownload().collect { counterValue ->
                if (counterValue != 100) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            updateSoftwarePercentageDownload = "($counterValue%)"
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            downloadControllerRepository.getStatusOfDownload().collect { status ->

                when (status) {
                    is DownloadController.DownloadControllerDownloadStatus.DOWNLOADING -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                updateSoftwareRequestStatus = UpdateSoftwareRequestStatus.LOADING,
                                updateSoftwareDownloadingStatus = UpdateSoftwareDownloadingStatus.LOADING,
                                updateSoftwareDialogText = R.string.update_software_dialog_text_downloading,
                                updateSoftwarePercentageDownload = ""
                            )
                        }
                    }

                    is DownloadController.DownloadControllerDownloadStatus.COMPLETE -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                updateSoftwareRequestStatus = UpdateSoftwareRequestStatus.SUCCESS,
                                updateSoftwareDownloadingStatus = UpdateSoftwareDownloadingStatus.SUCCESS,
                                updateSoftwareDialogText = R.string.update_software_dialog_downloaded,
                                updateSoftwarePercentageDownload = ""
                            )
                        }
                        appEventDownloadUpdate("download of new version is complete")
                    }

                    is DownloadController.DownloadControllerDownloadStatus.CANCEL -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                updateSoftwareRequestStatus = UpdateSoftwareRequestStatus.LOADING,
                                updateSoftwareDownloadingStatus = UpdateSoftwareDownloadingStatus.LOADING,
                                updateSoftwareDialogText = R.string.update_software_dialog_text_checking_for_updates,
                                updateSoftwarePercentageDownload = ""
                            )
                        }
                        appEventDownloadUpdate("download of new version is cancel")
                    }

                    is DownloadController.DownloadControllerDownloadStatus.FAIL -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                updateSoftwareRequestStatus = UpdateSoftwareRequestStatus.FAIL,
                                updateSoftwareDialogText = R.string.unexpected_error,
                                updateSoftwarePercentageDownload = ""
                            )
                        }
                        updateJob?.cancel()
                        appEventDownloadUpdate("download of new version is fail")
                    }
                }
            }
        }
    }

    fun closeUpdateDialog() {

        updateJob?.cancel()

        _uiState.update { currentState ->
            currentState.copy(
                updateSoftwareRequestStatus = UpdateSoftwareRequestStatus.LOADING,
                updateSoftwareDownloadingStatus = UpdateSoftwareDownloadingStatus.LOADING,
                updateSoftwareDialogText = R.string.update_software_dialog_text_checking_for_updates,
            )
        }

        viewModelScope.launch {
            downloadControllerRepository.cancelProcessOfDownload()
        }

    }

    /** @see SharedPreferencesRepo.mSchedulerServerDomain */
    var mSchedulerServerDomain: Pair<String, Int>
        get() = sharedPreferencesRepo.mSchedulerServerDomain
        set(value) {
            sharedPreferencesRepo.mSchedulerServerDomain = value
        }

    /** @see SharedPreferencesRepo.orgId */
    var orgId: String
        get() = sharedPreferencesRepo.orgId
        set(value) {
            sharedPreferencesRepo.orgId = value
        }

    /** @see SharedPreferencesRepo.doctors */
    var doctors: Set<String>
        get() = sharedPreferencesRepo.doctors
        set(value) {
            sharedPreferencesRepo.doctors = value
        }

    /** @see SharedPreferencesRepo.rooms */
    var rooms: Set<String>
        get() = sharedPreferencesRepo.rooms
        set(value) {
            sharedPreferencesRepo.rooms = value
        }

    /** @see SharedPreferencesRepo.formatCheckedList */
    var formatCheckedList: List<CreateAppointmentRequest.NationalIdFormat>
        get() = sharedPreferencesRepo.formatCheckedList
        set(value) {
            sharedPreferencesRepo.formatCheckedList = value
        }

    private var tempFormatCheckedList: List<CreateAppointmentRequest.NationalIdFormat>? = null
    var _tempFormatCheckedList: List<CreateAppointmentRequest.NationalIdFormat>
        get() {
            return tempFormatCheckedList ?: formatCheckedList
        }
        set(value) {
            tempFormatCheckedList = value
        }

    fun clearTempFormatCheckedList() {
        tempFormatCheckedList = null
    }

    /** @see SharedPreferencesRepo.showInsertNHICardAnimation */
    var showInsertNHICardAnimation: Boolean
        get() = sharedPreferencesRepo.showInsertNHICardAnimation
        set(value) {
            sharedPreferencesRepo.showInsertNHICardAnimation = value
        }

    /** @see SharedPreferencesRepo.checkInItemList */
    var checkInItemList: List<EditCheckInItemDialog.EditCheckInItem>
        get() = sharedPreferencesRepo.checkInItemList
        set(value) {
            sharedPreferencesRepo.checkInItemList = value
        }

    /** @see SharedPreferencesRepo.deptId */
    var deptId: Set<String>
        get() = sharedPreferencesRepo.deptId
        set(value) {
            sharedPreferencesRepo.deptId = value
        }

    /** @see SharedPreferencesRepo.queueingBoardSetting */
    var queueingBoardSettings: QueuingBoardSettingModel
        get() = sharedPreferencesRepo.queueingBoardSetting
        set(value) {
            sharedPreferencesRepo.queueingBoardSetting = value
        }

    /** @see SharedPreferencesRepo.queueingMachineSetting */
    var queueingMachineSettings: QueueingMachineSettingModel
        get() = sharedPreferencesRepo.queueingMachineSetting
        set(value) {
            sharedPreferencesRepo.queueingMachineSetting = value
        }


    /** @see SharedPreferencesRepo.automaticAppointmentSetting */
    var automaticAppointmentSetting: AutomaticAppointmentSettingModel
        get() = sharedPreferencesRepo.automaticAppointmentSetting
        set(value) {
            sharedPreferencesRepo.automaticAppointmentSetting = value
        }

    /** @see SharedPreferencesRepo.language */
    var language: String
        get() = sharedPreferencesRepo.language
        set(value) {
            sharedPreferencesRepo.language = value
        }

    /** @see SharedPreferencesRepo.machineTitle */
    var machineTitle: String
        get() = sharedPreferencesRepo.machineTitle
        set(value) {
            sharedPreferencesRepo.machineTitle = value
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
        .addInterceptor(LogInterceptor())
        .build()

    @OptIn(ExperimentalSerializationApi::class)
    private val serverRepo: ServerRepository
        get() {
            val retrofit = Retrofit.Builder()
                .client(client)
                .baseUrl(sharedPreferencesRepo.mSchedulerServerDomain.first)
                .addConverterFactory(format.asConverterFactory(MediaType.get("application/json")))
                .build()

            return ServerRepository(
                retrofit.create(ApiService::class.java)
            )
        }

    /** =======================================
     *          Log Record functions
     *  ======================================= */


    fun userChangeUiSetting(
        settingItemTitle: String,
        originalMachineTitle: String,
        originalValueIsShowInsertNHICardAnimation: Boolean,
        originalValue: EditCheckInItemDialog.EditCheckInItems,
        changeMachineTitle: String,
        changeValue: EditCheckInItemDialog.EditCheckInItems,
        changeIsShowInsertNHICardAnimation: Boolean
    ) {
        viewModelScope.launch {
            settingsEventLogger.logUserChangeUiSettingItem(
                itemTitle = settingItemTitle,
                originalMachineTitle = originalMachineTitle,
                originalValue = originalValue,
                originalValueIsShowInsertNHICardAnimation = originalValueIsShowInsertNHICardAnimation,
                changeMachineTitle = changeMachineTitle,
                changeValue = changeValue,
                changeIsShowInsertNHICardAnimation = changeIsShowInsertNHICardAnimation
            )
        }
    }

    fun userChangeDomainSetting(
        settingItemTitle: String,
        originalUrl: String,
        originalSelect: Int,
        newUrl: String,
        newSelect: Int
    ) {
        viewModelScope.launch {
            settingsEventLogger.logUserChangeDomainSettingItem(
                itemTitle = settingItemTitle,
                originalUrl = originalUrl,
                originalSelect = originalSelect,
                newUrl = newUrl,
                newSelect = newSelect
            )
        }
    }


    fun userChangeSettingItem(
        settingItemTitle: String,
        originalValue: String,
        newValue: String,
    ) {
        viewModelScope.launch {
            settingsEventLogger.logUserChangeSettingItem(
                itemTitle = settingItemTitle, originalValue = originalValue, newValue = newValue
            )
        }
    }

    fun userChangeQueueingBoardSetting(
        settingItemTitle: String,
        originalValue: QueuingBoardSettingModel,
        newValue: QueuingBoardSettingModel,
    ) {
        viewModelScope.launch {
            settingsEventLogger.logUserChangeQueueingBoardSettingItem(
                itemTitle = settingItemTitle,
                originalValue = originalValue,
                newValue = newValue
            )
        }
    }

    fun userChangeQueueingMachineSetting(
        settingItemTitle: String,
        originalValue: QueueingMachineSettingModel,
        newValue: QueueingMachineSettingModel,
    ) {
        viewModelScope.launch {
            settingsEventLogger.logUserChangeQueueingMachineSettingItem(
                itemTitle = settingItemTitle,
                originalValue = originalValue,
                newValue = newValue
            )
        }
    }

    fun userChangeCheckedListSetting(
        settingItemTitle: String,
        originalValue: List<CreateAppointmentRequest.NationalIdFormat>,
        newValue: List<CreateAppointmentRequest.NationalIdFormat>,
    ) {
        viewModelScope.launch {
            settingsEventLogger.logUserChangeFormatCheckSettingItem(
                itemTitle = settingItemTitle,
                originalValue = originalValue,
                newValue = newValue
            )
        }
    }

    fun userChangeAutomaticAppointmentSetting(
        settingItemTitle: String,
        originalValue: AutomaticAppointmentSettingModel,
        newValue: AutomaticAppointmentSettingModel,
    ) {
        viewModelScope.launch {
            settingsEventLogger.logUserChangeAutomaticAppointmentSettingItem(
                itemTitle = settingItemTitle,
                originalValue = originalValue,
                newValue = newValue
            )
        }
    }

    fun userSelectSoftwareUpdateSetting(
        settingItemTitle: String,
        permissions: List<String>,
    ) {
        viewModelScope.launch {
            settingsEventLogger.logUserSelectSoftwareUpdateSettingItemItem(
                itemTitle = settingItemTitle,
                permission = permissions
            )
        }
    }

    fun userSelectSoftwareUpdateSetting(
        settingItemTitle: String,
        currentVersion: String
    ) {
        viewModelScope.launch {
            settingsEventLogger.logUserSelectSoftwareUpdateSettingItemItem(
                itemTitle = settingItemTitle,
                currentVersion = currentVersion
            )
        }
    }

    fun userOpenDeviceSetting() {
        viewModelScope.launch {
            settingsEventLogger.logUserSelectOpenSettingsOfDevice()
        }
    }

    fun userCloseApplication() {
        viewModelScope.launch {
            settingsEventLogger.logUserCloseApp()
        }
    }

    private fun responseCheckForUpdate(response: Response<ControllerAppVersionResponse>) {
        viewModelScope.launch {
            settingsEventLogger.logCheckUpdateResponse(response)
        }
    }

    private fun appEventDownloadUpdate(eventName: String) {
        viewModelScope.launch {
            settingsEventLogger.logAppEventDownloadUpdate(eventName)
        }
    }
}