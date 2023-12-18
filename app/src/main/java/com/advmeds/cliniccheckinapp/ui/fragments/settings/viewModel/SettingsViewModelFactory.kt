package com.advmeds.cliniccheckinapp.ui.fragments.settings.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.advmeds.cliniccheckinapp.repositories.DownloadControllerRepository
import com.advmeds.cliniccheckinapp.ui.fragments.settings.eventLogger.SettingsEventLogger

class SettingsViewModelFactory(
    private val application: Application,
    private val downloadControllerRepository: DownloadControllerRepository,
    private val settingsEventLogger: SettingsEventLogger
) : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SettingsViewModel(
            application = application,
            downloadControllerRepository = downloadControllerRepository,
            settingsEventLogger
        ) as T
    }
}