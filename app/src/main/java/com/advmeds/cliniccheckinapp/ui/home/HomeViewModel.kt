package com.advmeds.cliniccheckinapp.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharePreferences.QueueingMachineSettingModel
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo

class HomeViewModel(application: Application) : AndroidViewModel(application) {
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

    var doctorIds: List<String>
        get() = sharedPreferencesRepo.doctorIds
        set(value) {
            sharedPreferencesRepo.doctorIds = value
        }

    var roomIds: List<String>
        get() = sharedPreferencesRepo.roomIds
        set(value) {
            sharedPreferencesRepo.roomIds = value
        }

    var deptId: String
        get() = sharedPreferencesRepo.deptId
        set(value) {
            sharedPreferencesRepo.deptId = value
        }

    var queueingMachineSettings: QueueingMachineSettingModel
        get() = sharedPreferencesRepo.queueingMachineSetting
        set(value) {
            sharedPreferencesRepo.queueingMachineSetting = value
        }



    var language: String
        get() = sharedPreferencesRepo.language
        set(value) {
            sharedPreferencesRepo.language = value
        }
            
    /** @see SharedPreferencesRepo.logo */
    var logo: String?
        get() = sharedPreferencesRepo.logo
        set(value) {
            sharedPreferencesRepo.logo = value
        }
}