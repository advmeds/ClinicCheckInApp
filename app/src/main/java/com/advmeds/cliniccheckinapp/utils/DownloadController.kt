package com.advmeds.cliniccheckinapp.utils

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.net.Uri
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class DownloadController(
    private val contextApp: Context
) {

    private val _downloadProgressFlow: MutableStateFlow<Int> = MutableStateFlow(0)

    var downloadStatus = MutableStateFlow<DownloadControllerDownloadStatus>(
        DownloadControllerDownloadStatus.DOWNLOADING
    )
        private set


    fun getDownloadProgressFlow(): Flow<Int> = _downloadProgressFlow.asStateFlow()
    fun getStatusOfDownloadFlow(): Flow<DownloadControllerDownloadStatus> =
        downloadStatus.asStateFlow()

    private var downloadId = -1L
    private var isDownloading = false

    companion object {

        private const val TAG = "check---:DownloadC"

        private const val FILE_NAME = "mSchedulerCheckIn"
        private const val FOLDER_NAME = "mSchedulerCheckIn"
        private const val FILE_BASE_PATH = "file://"
        private const val APP_INSTALL_PATH = "application/vnd.android.package-archive"

        fun getFileName() = "$FILE_NAME.apk"
    }

    suspend fun enqueueDownload(url: String, version: String) {

        downloadStatus.emit(DownloadControllerDownloadStatus.DOWNLOADING)

        val downloadManager =
            contextApp.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(url)

        val request = DownloadManager.Request(downloadUri)
            .setTitle(getFileName())
            .setDescription("Downloading update...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setMimeType(APP_INSTALL_PATH)

        Log.d(TAG, "enqueueDownload: apk uri ${contextApp.cacheDir.deleteRecursively()}")
        val privateDir: File? = contextApp.externalCacheDir

        if (privateDir != null) {

            val apkFile = File(privateDir, getFileName())
            request.setDestinationUri(Uri.fromFile(apkFile))

            Log.d(TAG, "enqueueDownload: apk uri ${Uri.fromFile(apkFile)}")
            Log.d(TAG, "enqueueDownload: downloading in private folder}")
        } else {

            val destinationDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            val destinationPath =
                "${destinationDir.absolutePath}/$FOLDER_NAME/${getFileName()}"

            createFolderIfItDoesNotExist(destinationDir)

            deletePreviousApkIfExist(destinationPath)

            request.setDestinationUri(Uri.parse("$FILE_BASE_PATH$destinationPath"))

            Log.d(TAG, "enqueueDownload: apk uri ${Uri.parse("$FILE_BASE_PATH$destinationPath")}")
            Log.d(TAG, "enqueueDownload: downloading in public folder}")
        }

        downloadId = downloadManager.enqueue(request)

        downloadWithProgress(downloadManager)
    }

    private suspend fun downloadWithProgress(manager: DownloadManager) {
        isDownloading = true
        while (isDownloading) {
            try {
                val query = DownloadManager.Query().apply {
                    setFilterById(downloadId)
                }

                val cursor = manager.query(query)
                cursor.moveToFirst()

                val bytesDownloadedColumnIndex =
                    cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val bytesTotalColumnIndex =
                    cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

                val bytesDownloaded =
                    if (bytesDownloadedColumnIndex != -1) cursor.getInt(bytesDownloadedColumnIndex) else 0
                val bytesTotal =
                    if (bytesTotalColumnIndex != -1) cursor.getInt(bytesTotalColumnIndex) else 0
                val status = if (statusColumnIndex != -1) cursor.getInt(statusColumnIndex) else -1

                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    isDownloading = false
                    downloadStatus.emit(DownloadControllerDownloadStatus.COMPLETE)
                } else if (status == DownloadManager.STATUS_FAILED) {
                    isDownloading = false
                    downloadStatus.emit(DownloadControllerDownloadStatus.FAIL)

                    val reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                    Log.e(TAG, "Reason: $reason")
                }

                if (bytesTotal > 0) {
                    val dlProgress = ((bytesDownloaded.toDouble() / bytesTotal) * 100).toInt()

                    Log.d(TAG, "downloadWithProgress: $dlProgress")

                    _downloadProgressFlow.value = dlProgress
                }

                Log.d(TAG, "status - ${statusMessage(cursor)}")

                cursor.close()

                delay(2000)
            } catch (e: CursorIndexOutOfBoundsException) {
                cancelDownload()
            }
        }
    }

    suspend fun cancelDownload() {
        if (downloadId != -1L) {
            val downloadManager =
                contextApp.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            downloadManager.remove(downloadId)
            downloadId = -1
        }

        isDownloading = false
        downloadStatus.emit(DownloadControllerDownloadStatus.CANCEL)
    }

    private fun createFolderIfItDoesNotExist(destinationDir: File?) {
        val folder = File(destinationDir, FOLDER_NAME)
        if (!folder.exists()) {
            folder.mkdirs()
        }
    }


    private fun deletePreviousApkIfExist(destinationPath: String) {
        val file = File(destinationPath)
        if (file.exists()) {
            file.delete()
        }
    }


    private fun statusMessage(c: Cursor): String {
        var msg = "???"


        val cursorValue = c.getColumnIndex(DownloadManager.COLUMN_STATUS)

        msg =
            when (if (cursorValue > 0) c.getInt(cursorValue) else 0) {
                DownloadManager.STATUS_FAILED -> "Download failed!"
                DownloadManager.STATUS_PAUSED -> "Download paused!"
                DownloadManager.STATUS_PENDING -> "Download pending!"
                DownloadManager.STATUS_RUNNING -> "Download in progress!"
                DownloadManager.STATUS_SUCCESSFUL -> "Download complete!"
                else -> "Download is nowhere in sight"
            }
        return msg
    }

    sealed interface DownloadControllerDownloadStatus {
        object COMPLETE : DownloadControllerDownloadStatus
        object CANCEL : DownloadControllerDownloadStatus
        object FAIL : DownloadControllerDownloadStatus
        object DOWNLOADING : DownloadControllerDownloadStatus
    }
}