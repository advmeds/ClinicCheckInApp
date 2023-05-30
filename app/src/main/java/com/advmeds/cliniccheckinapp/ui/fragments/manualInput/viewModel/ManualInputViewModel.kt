package com.advmeds.cliniccheckinapp.ui.fragments.manualInput.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo

class ManualInputViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferencesRepo = SharedPreferencesRepo.getInstance(getApplication())

    /** @see SharedPreferencesRepo.logoUrl */
    var logoUrl: String?
        get() = sharedPreferencesRepo.logoUrl
        set(value) {
            sharedPreferencesRepo.logoUrl = value
        }

    /** @see SharedPreferencesRepo.formatCheckedList */
    var formatCheckedList: List<CreateAppointmentRequest.NationalIdFormat>
        get() = sharedPreferencesRepo.formatCheckedList
        set(value) {
            sharedPreferencesRepo.formatCheckedList = value
        }
}