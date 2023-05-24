package com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences

data class QueueingMachineSettingModel(
    val organization: Boolean,
    val doctor: Boolean,
    val dept: Boolean,
    val time: Boolean,
) {
    fun isSame(queueingMachineSettingModel: QueueingMachineSettingModel): Boolean {
        return queueingMachineSettingModel.organization == organization &&
                queueingMachineSettingModel.doctor == doctor &&
                queueingMachineSettingModel.dept == dept &&
                queueingMachineSettingModel.time == time
    }
}