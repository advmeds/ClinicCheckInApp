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
) {
    fun isSame(queueingMachineSettingModel: QueueingMachineSettingModel): Boolean {
        return queueingMachineSettingModel.isEnabled == isEnabled &&
                queueingMachineSettingModel.organization == organization &&
                queueingMachineSettingModel.doctor == doctor &&
                queueingMachineSettingModel.dept == dept &&
                queueingMachineSettingModel.time == time &&
                queueingMachineSettingModel.isOneTicket == isOneTicket
    }

    companion object {
        fun isAllParamAreFalse(
            organization: Boolean,
            doctor: Boolean,
            dept: Boolean,
            time: Boolean,
        ) = !(organization || doctor || dept || time)
    }
}