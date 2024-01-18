package com.advmeds.cliniccheckinapp.ui.fragments.settings.eventLogger

import com.advmeds.cliniccheckinapp.dialog.EditCheckInItemDialog
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.ControllerAppVersionResponse
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.AutomaticAppointmentSettingModel
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueueingMachineSettingModel
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueuingBoardSettingModel
import com.advmeds.cliniccheckinapp.repositories.AnalyticsRepository
import retrofit2.Response

class SettingsEventLogger(
    private val analyticsRepository: AnalyticsRepository
) {

    // User events

    suspend fun logUserChangeUiSettingItem(
        itemTitle: String,
        originalMachineTitle: String,
        originalValue: EditCheckInItemDialog.EditCheckInItems,
        changeMachineTitle: String,
        changeValue: EditCheckInItemDialog.EditCheckInItems
    ) {
        val map = mutableMapOf<String, Any>()
        setChangeSettingProperties(map)

        map["item title"] = itemTitle
        map["original machine title"] = originalMachineTitle
        map["original value"] = originalValue
        map["change machine title"] = changeMachineTitle
        map["change value"] = changeValue

        analyticsRepository.sendEvent("change the setting item: $itemTitle", map)
    }

    suspend fun logUserChangeDomainSettingItem(
        itemTitle: String,
        originalUrl: String,
        originalSelect: Int,
        newUrl: String,
        newSelect: Int
    ) {
        val map = mutableMapOf<String, Any>()
        setChangeSettingProperties(map)

        map["item title"] = itemTitle
        map["original domain"] = originalUrl
        map["original select radio button"] = originalSelect
        map["new domain"] = newUrl
        map["new select radio button"] = newSelect

        analyticsRepository.sendEvent("change the setting item: $itemTitle", map)
    }

    suspend fun logUserChangeSettingItem(
        itemTitle: String,
        originalValue: String,
        newValue: String,
    ) {
        val map = mutableMapOf<String, Any>()
        setChangeSettingProperties(map)

        map["item title"] = itemTitle
        map["original value"] = originalValue
        map["new value"] = newValue

        analyticsRepository.sendEvent("change the setting item: $itemTitle", map)
    }

    suspend fun logUserChangeQueueingBoardSettingItem(
        itemTitle: String,
        originalValue: QueuingBoardSettingModel,
        newValue: QueuingBoardSettingModel,
    ) {
        val map = mutableMapOf<String, Any>()
        setChangeSettingProperties(map)

        map["item title"] = itemTitle
        map["original queuing board setting model"] = originalValue
        map["new queuing board setting model"] = newValue

        analyticsRepository.sendEvent("change the setting item: $itemTitle", map)
    }

    suspend fun logUserChangeQueueingMachineSettingItem(
        itemTitle: String,
        originalValue: QueueingMachineSettingModel,
        newValue: QueueingMachineSettingModel,
    ) {
        val map = mutableMapOf<String, Any>()
        setChangeSettingProperties(map)

        map["item title"] = itemTitle
        map["original queuing board machine model"] = originalValue
        map["new queuing board machine model"] = newValue

        analyticsRepository.sendEvent("change the setting item: $itemTitle", map)
    }

    suspend fun logUserChangeFormatCheckSettingItem(
        itemTitle: String,
        originalValue: List<CreateAppointmentRequest.NationalIdFormat>,
        newValue: List<CreateAppointmentRequest.NationalIdFormat>,
    ) {
        val map = mutableMapOf<String, Any>()
        setChangeSettingProperties(map)

        map["item title"] = itemTitle
        map["original check in setting"] = originalValue
        map["new check in setting"] = newValue

        analyticsRepository.sendEvent("change the setting item: $itemTitle", map)
    }

    suspend fun logUserChangeAutomaticAppointmentSettingItem(
        itemTitle: String,
        originalValue: AutomaticAppointmentSettingModel,
        newValue: AutomaticAppointmentSettingModel,
    ) {
        val map = mutableMapOf<String, Any>()
        setChangeSettingProperties(map)

        map["item title"] = itemTitle
        map["original automatic appointment"] = originalValue
        map["new automatic appointment"] = newValue

        analyticsRepository.sendEvent("change the setting item: $itemTitle", map)
    }

    suspend fun logUserSelectSoftwareUpdateSettingItemItem(
        itemTitle: String,
        permission: List<String>
    ) {
        val map = mutableMapOf<String, Any>()
        map[AnalyticsRepository.SOURCE_SCREEN] = "setting screen"
        map[AnalyticsRepository.SOURCE_ACTION] = "select the setting item update app"

        map["item title"] = itemTitle
        map["permissions that user need to turn on"] = permission

        analyticsRepository.sendEvent("change the setting item: $itemTitle", map)
    }

    suspend fun logUserSelectSoftwareUpdateSettingItemItem(
        itemTitle: String,
        currentVersion: String
    ) {
        val map = mutableMapOf<String, Any>()
        map[AnalyticsRepository.SOURCE_SCREEN] = "setting screen"
        map[AnalyticsRepository.SOURCE_ACTION] = "select the setting item update app"

        map["item title"] = itemTitle
        map["current version of application"] = currentVersion

        analyticsRepository.sendEvent("change the setting item: $itemTitle", map)
    }

    suspend fun logUserSelectOpenSettingsOfDevice() {
        val map = mutableMapOf<String, Any>()
        map[AnalyticsRepository.SOURCE_SCREEN] = "setting screen"
        map[AnalyticsRepository.SOURCE_ACTION] =
            "user select open system settings of the device, setting item"
        analyticsRepository.sendEvent("open device's settings", map)
    }

    suspend fun logUserCloseApp() {
        val map = mutableMapOf<String, Any>()
        map[AnalyticsRepository.SOURCE_SCREEN] = "setting screen"
        map[AnalyticsRepository.SOURCE_ACTION] = "select exit app setting item"
        analyticsRepository.sendEvent("close application", map)
    }

    // App Events

    suspend fun logCheckUpdateResponse(response: Response<ControllerAppVersionResponse>) {
        val map = mutableMapOf<String, Any>()
        map[AnalyticsRepository.SOURCE_SCREEN] = "setting screen"
        map[AnalyticsRepository.SOURCE_ACTION] = "server response for check update request"

        if (response.isSuccessful) {
            map["check update result"] = "onSuccess"
            map["check update data"] = response
        } else {
            map["check update result"] = "onFailure"
            map["check update data"] = response
        }

        analyticsRepository.sendEvent("response: Check Update", map)
    }

    suspend fun logAppEventDownloadUpdate(eventName: String) {
        val map = mutableMapOf<String, Any>()
        map[AnalyticsRepository.SOURCE_SCREEN] = "setting screen"
        map[AnalyticsRepository.SOURCE_ACTION] = "ViewModel Setting Download State"

        analyticsRepository.sendEvent(eventName, map)
    }

    private fun setChangeSettingProperties(params: MutableMap<String, Any>) {
        params[AnalyticsRepository.SOURCE_SCREEN] = "setting screen"
        params[AnalyticsRepository.SOURCE_ACTION] = "change the setting item"
    }
}