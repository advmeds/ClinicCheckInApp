package com.advmeds.cliniccheckinapp.ui.fragments.settings.viewModel

import com.advmeds.cliniccheckinapp.R

data class SettingsUiState(

    val openSoftwareUpdateNotificationDialog: Boolean = false,

    val updateSoftwareNotificationUrl: String = "",
    val updateSoftwareNotificationVersion: String = "",

    val updateSoftwareRequestStatus: UpdateSoftwareRequestStatus = UpdateSoftwareRequestStatus.LOADING,
    val updateSoftwareDownloadingStatus: UpdateSoftwareDownloadingStatus =  UpdateSoftwareDownloadingStatus.LOADING,
    val updateSoftwareDialogText: Int = R.string.update_software_dialog_text_checking_for_updates,
    val updateSoftwarePercentageDownload: String = "",

    )


/**
 * UI state for the check controller version
 */
sealed interface UpdateSoftwareRequestStatus {
    object LOADING : UpdateSoftwareRequestStatus
    object SUCCESS : UpdateSoftwareRequestStatus
    object FAIL : UpdateSoftwareRequestStatus
}

/**
 * UI state for downloading new version
 */
sealed interface UpdateSoftwareDownloadingStatus {
    object LOADING : UpdateSoftwareDownloadingStatus
    object SUCCESS: UpdateSoftwareDownloadingStatus
    object FAIL: UpdateSoftwareDownloadingStatus
}