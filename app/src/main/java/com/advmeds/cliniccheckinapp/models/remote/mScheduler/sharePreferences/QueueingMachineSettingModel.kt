package com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharePreferences

data class QueueingMachineSettingModel(
    val organization: Boolean,
    val doctor: Boolean,
    val dept: Boolean,
    val time: Boolean,
) {
    fun isNotSame(queueingMachineSettingModel: QueueingMachineSettingModel) : Boolean {

        var same = false

        if (organization != queueingMachineSettingModel.organization)
            same = true

        if (doctor != queueingMachineSettingModel.doctor)
            same = true

        if (dept != queueingMachineSettingModel.dept)
            same = true

        if (time != queueingMachineSettingModel.time)
            same = true

        return same
    }
}
