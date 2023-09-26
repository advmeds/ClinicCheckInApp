package com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences

import kotlinx.serialization.Serializable

@Serializable
data class QueueingMachineSettingModel(
    val isEnabled: Boolean,
    val organization: Boolean,
    val doctor: Boolean,
    val dept: Boolean,
    val time: Boolean,
    val isOneTicket: Boolean,
    val textSize: MillimeterSize,
) {
    fun isSame(queueingMachineSettingModel: QueueingMachineSettingModel): Boolean {
        return queueingMachineSettingModel.isEnabled == isEnabled &&
                queueingMachineSettingModel.organization == organization &&
                queueingMachineSettingModel.doctor == doctor &&
                queueingMachineSettingModel.dept == dept &&
                queueingMachineSettingModel.time == time &&
                queueingMachineSettingModel.isOneTicket == isOneTicket &&
                queueingMachineSettingModel.textSize == textSize
    }

    companion object {
        fun isAllParamAreFalse(
            organization: Boolean,
            doctor: Boolean,
            dept: Boolean,
            time: Boolean,
        ) = !(organization || doctor || dept || time)
    }
    @Serializable
    enum class MillimeterSize {
        FIFTY_SEVEN_MILLIMETERS,
        SEVENTY_SIX_MILLIMETERS;
    }

}