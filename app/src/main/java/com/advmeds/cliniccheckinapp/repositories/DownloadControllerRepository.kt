package com.advmeds.cliniccheckinapp.repositories

import com.advmeds.cliniccheckinapp.utils.DownloadController
import kotlinx.coroutines.flow.Flow

class DownloadControllerRepository(
    private val downloadController: DownloadController
) {

    fun getProgressOfDownload(): Flow<Int> = downloadController.getDownloadProgressFlow()

    suspend fun startProcessOfDownload(url: String, version: String) =
        downloadController.enqueueDownload(url, version)

    fun getStatusOfDownload(): Flow<DownloadController.DownloadControllerDownloadStatus> =
        downloadController.getStatusOfDownloadFlow()

    suspend fun cancelProcessOfDownload() = downloadController.cancelDownload()
}