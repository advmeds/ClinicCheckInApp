package com.advmeds.cliniccheckinapp.ui.fragments.settings.eventLogger

import com.advmeds.cliniccheckinapp.dialog.EditCheckInItemDialog
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueueingMachineSettingModel
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueuingBoardSettingModel
import com.advmeds.cliniccheckinapp.repositories.AnalyticsRepository

class SettingsEventLogger(
    private val analyticsRepository: AnalyticsRepository
) {

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

    suspend fun logUserChangeQueueingMachineSettingItem(
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

    private fun setChangeSettingProperties(params: MutableMap<String, Any>) {
        params[AnalyticsRepository.SOURCE_SCREEN] = "setting screen"
        params[AnalyticsRepository.SOURCE_ACTION] = "change the setting item"
    }
}