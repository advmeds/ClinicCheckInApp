package com.advmeds.cliniccheckinapp.ui.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo

class ManualInputViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferencesRepo = SharedPreferencesRepo.getInstance(getApplication())

    /** @see SharedPreferencesRepo.logoUrl */
    var logoUrl: String?
        get() = sharedPreferencesRepo.logoUrl
        set(value) {
            sharedPreferencesRepo.logoUrl = value
        }
}