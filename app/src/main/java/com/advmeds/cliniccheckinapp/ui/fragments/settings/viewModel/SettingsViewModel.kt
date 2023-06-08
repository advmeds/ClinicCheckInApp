package com.advmeds.cliniccheckinapp.ui.fragments.settings.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.advmeds.cliniccheckinapp.dialog.EditCheckInItemDialog
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueueingMachineSettingModel
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueuingBoardSettingModel
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferencesRepo = SharedPreferencesRepo.getInstance(getApplication())

    /** @see SharedPreferencesRepo.mSchedulerServerDomain */
    var mSchedulerServerDomain: String
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
}