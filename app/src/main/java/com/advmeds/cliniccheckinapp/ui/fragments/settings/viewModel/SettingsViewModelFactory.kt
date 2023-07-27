package com.advmeds.cliniccheckinapp.ui.fragments.settings.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.advmeds.cliniccheckinapp.repositories.DownloadControllerRepository

class SettingsViewModelFactory(
    private val application: Application,
    private val downloadControllerRepository: DownloadControllerRepository
) : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SettingsViewModel(
            application = application,
            downloadControllerRepository = downloadControllerRepository
        ) as T
    }
}