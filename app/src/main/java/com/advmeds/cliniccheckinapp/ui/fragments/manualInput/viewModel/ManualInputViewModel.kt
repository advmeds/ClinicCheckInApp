package com.advmeds.cliniccheckinapp.ui.fragments.manualInput.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo
import com.advmeds.cliniccheckinapp.ui.fragments.manualInput.eventLogger.ManualInputEventLogger
import kotlinx.coroutines.launch

class ManualInputViewModel(
    application: Application,
    private val manualInputEventLogger: ManualInputEventLogger
) : AndroidViewModel(application) {
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


    /** @see SharedPreferencesRepo.machineTitle */
    var machineTitle: String
        get() = sharedPreferencesRepo.machineTitle
        set(value) {
            sharedPreferencesRepo.machineTitle = value
        }


    /** =======================================
     *          Log Record functions
     *  ======================================= */

    fun userSendManualInputData(manualInputData: String) {
        viewModelScope.launch {
            manualInputEventLogger.logUserSendTheManualInputData(manualInputData)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class ManualInputViewModelFactory(
    private val application: Application,
    private val manualInputEventLogger: ManualInputEventLogger
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (ManualInputViewModel(application, manualInputEventLogger) as T)
}