package com.advmeds.cliniccheckinapp.ui.inputPage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo

class InputPageViewModel(application: Application) : AndroidViewModel(application) {
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

    /** @see SharedPreferencesRepo.logo */
    var logo: String?
        get() = sharedPreferencesRepo.logo
        set(value) {
            sharedPreferencesRepo.logo = value
        }
}