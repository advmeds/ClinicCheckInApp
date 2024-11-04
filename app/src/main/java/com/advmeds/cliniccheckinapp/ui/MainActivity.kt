package com.advmeds.cliniccheckinapp.ui

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.app.PendingIntent
import android.app.Presentation
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.media.AudioManager
import android.media.MediaRouter
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.advmeds.cardreadermodule.AcsResponseModel
import com.advmeds.cardreadermodule.UsbDeviceCallback
import com.advmeds.cardreadermodule.acs.usb.AcsUsbDevice
import com.advmeds.cardreadermodule.acs.usb.decoder.AcsUsbTWDecoder
import com.advmeds.cardreadermodule.castles.CastlesUsbDevice
import com.advmeds.cardreadermodule.rfpro.RFProDevice
import com.advmeds.cliniccheckinapp.BuildConfig
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.ActivityMainBinding
import com.advmeds.cliniccheckinapp.dialog.CheckingDialogFragment
import com.advmeds.cliniccheckinapp.dialog.ErrorDialogFragment
import com.advmeds.cliniccheckinapp.dialog.ScheduleListDialogFragment
import com.advmeds.cliniccheckinapp.dialog.SuccessDialogFragment
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.ApiError
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.CreateAppointmentResponse
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetScheduleResponse
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.AutomaticAppointmentMode
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueueingMachineSettingModel
import com.advmeds.cliniccheckinapp.repositories.AnalyticsRepositoryImpl
import com.advmeds.cliniccheckinapp.repositories.RoomRepositories
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo
import com.advmeds.cliniccheckinapp.utils.CheckNetworkConnection
import com.advmeds.cliniccheckinapp.utils.DownloadController
import com.advmeds.cliniccheckinapp.utils.toCharSequence
import com.advmeds.cliniccheckinapp.utils.zipWith
import com.advmeds.printerlib.usb.BPT3XPrinterService
import com.advmeds.printerlib.usb.EP360CPrintService
import com.advmeds.printerlib.usb.UsbPrinterService
import com.advmeds.printerlib.utils.EP360CPrinterBuffer
import com.advmeds.printerlib.utils.PrinterBuffer
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.reflect.full.primaryConstructor


class MainActivity : AppCompatActivity() {
    companion object {
        private const val USB_PERMISSION = "${BuildConfig.APPLICATION_ID}.USB_PERMISSION"
//        private const val SCAN_TIME_OUT: Long = 15
    }

    private val checkNetworkConnectivity: CheckNetworkConnection by lazy {
        CheckNetworkConnection(application)
    }

    private val navHostFragment: FragmentContainerView by lazy {
        findViewById(R.id.nav_host_fragment)
    }

    private val reconnectingLayout: RelativeLayout by lazy {
        findViewById(R.id.layout_reconnecting)
    }

    private var isInternet = false
    private val viewModel by viewModels<MainViewModel> {
        MainViewModelFactory(
            application = application,
            mainEventLogger = MainEventLogger(
                AnalyticsRepositoryImpl.getInstance(
                    RoomRepositories.eventsRepository,
                    SharedPreferencesRepo.getInstance(this)
                )
            )
        )
    }

    private lateinit var binding: ActivityMainBinding

    var dialog: AppCompatDialogFragment? = null
    private var presentation: Presentation? = null

    private val detectUsbDeviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val usbDevice = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
            Timber.d("current action: ${intent.action} with ${usbDevice?.productName.orEmpty()}")

            when (intent.action) {
                USB_PERMISSION -> {
                    acsUsbDevice.supportedDevice?.let {
                        acsUsbDevice.connectDevice(it)
                    }
                    ezUsbDevice.supportedDevice?.let {
                        ezUsbDevice.connectDevice(it)
                    }
                    rfProDevice.supportedDevice?.let {
                        rfProDevice.connect()
                    }
                    usbPrinterService?.supportedDevice?.let {
                        try {
                            usbPrinterService?.connectDevice(it)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Snackbar.make(
                                binding.root,
                                "Fail to connect the usb printer.",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    usbDevice?.let { connectUSBDevice(it) }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    when (usbDevice?.productId) {
                        acsUsbDevice.connectedDevice?.productId -> {
                            acsUsbDevice.disconnect()
                        }
                        ezUsbDevice.connectedDevice?.productId -> {
                            ezUsbDevice.disconnect()
                        }
                        usbPrinterService?.connectedDevice?.productId -> {
                            usbPrinterService?.disconnect()
                        }
                    }
                }
            }
        }
    }

    private lateinit var usbManager: UsbManager

    private lateinit var acsUsbDevice: AcsUsbDevice

    private lateinit var ezUsbDevice: CastlesUsbDevice

    private lateinit var rfProDevice: RFProDevice

    private val usbDeviceCallback = object : UsbDeviceCallback {
        override fun onCardPresent() {

        }

        override fun onCardAbsent() {
//            viewModel.cancelJobOnCardAbsent()
            dialog?.dismiss()
            dialog = null

//            viewModel.completeAllJobOnCardAbsentAfterAllProcessIsOver() {
//                dialog?.dismiss()
//                dialog = null
//            }

        }

        override fun onConnectDevice() {

        }

        override fun onFailToConnectDevice() {
            Snackbar.make(binding.root, "Fail to connect usb card reader.", Snackbar.LENGTH_LONG)
                .show()
        }

        override fun onReceiveResult(result: Result<AcsResponseModel>) {
            result.onSuccess {

                when (BuildConfig.BUILD_TYPE) {
                    "rende" -> {
                        createAppointment(
                            isCheckIn = false,
                            schedule = GetScheduleResponse.ScheduleBean.RENDE_DIVISION_ONLY,
                            patient = CreateAppointmentRequest.Patient(
                                nationalId = it.icId,
                                birthday = it.birthday?.let { dateBean -> "${dateBean.year}-${dateBean.month}-${dateBean.day}" }
                                    ?: "",
                                name = it.name
                            )
                        )
                    }
                    else -> {
                        getPatients(
                            nationalId = it.icId,
                            birth = it.birthday?.let { dateBean -> "${dateBean.year}-${dateBean.month}-${dateBean.day}" }
                                ?: "",
                            name = it.name
                        )
                    }
                }
            }.onFailure {
//                it.message?.let { it1 ->
//                    Snackbar.make(binding.root, it1, Snackbar.LENGTH_LONG).show()
//                }

                soundPool.play(
                    failCardInsertSoundId,
                    1f,
                    1f,
                    0,
                    0,
                    1f
                )


                dialog = ErrorDialogFragment(
                    title = getString(R.string.fail_to_check),
                    message = getString(R.string.fail_to_card_reading)
                )
                dialog?.showNow(supportFragmentManager, null)
            }
            viewModel.eventUserInsertCard(result)
        }
    }

//    /** 確認使用者授權的Callback */
//    private val bleForResult =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
//            if (result.values.find { !it } == null) {
//                enableBluetoothService()
//            }
//        }

//    /** Create a BroadcastReceiver for ACTION_STATE_CHANGED. */
//    private val detectBluetoothStateReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            when(intent.action) {
//                BluetoothAdapter.ACTION_STATE_CHANGED -> {
//                    // Discovery has found a device. Get the BluetoothDevice
//                    // object and its info from the Intent.
//                    val prevState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, 0)
//                    val currState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)
//                    Timber.d("prevState: $prevState, currState: $currState")
//
//                    when (currState) {
//                        BluetoothAdapter.STATE_ON -> {
//                            startScan()
//                        }
//                        BluetoothAdapter.STATE_TURNING_OFF -> {
//                            stopScan()
//                        }
//                        BluetoothAdapter.STATE_OFF -> {
//                            BluetoothAdapter.getDefaultAdapter().enable()
//                        }
//                    }
//                }
//            }
//        }
//    }

    // Create a BroadcastReceiver for ACTION_FOUND.
//    private val detectBluetoothDeviceReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            when(intent.action) {
//                BluetoothDevice.ACTION_FOUND -> {
//                    // Discovery has found a device. Get the BluetoothDevice
//                    // object and its info from the Intent.
//                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
//
//                    device ?: return
//
//                    Timber.d(device.name)
//
//                    if (device.name == "58Printer") {
//                        printService.connect(device)
//                    }
//                }
//            }
//        }
//    }

//    private lateinit var printService: BluetoothPrinterService

    var usbPrinterService: UsbPrinterService? = null

    private val reloadClinicDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.getClinicGuardian()
        }
    }

    private val presentationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isEnable =
                intent?.getBooleanExtra(SharedPreferencesRepo.CLINIC_PANEL_MODE_IS_ENABLED, false)

            if (isEnable == true)
                presentation?.show()
            else
                presentation?.dismiss()
        }
    }


    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                val apkUri = getDownloadedFileUri(downloadId)

                if (apkUri != null) {
                    if (context != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (!packageManager.canRequestPackageInstalls()) {
                                Toast.makeText(
                                    context,
                                    "Permission for installation unknown apk is denied",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return
                            }
                        }

                        // Permission already granted, proceed with installation
                        installAPK(Uri.parse(apkUri), context)
                    }
                }
            }
        }
    }

    private lateinit var soundPool: SoundPool
    private var successSoundId: Int = 0
    private var failSoundId: Int = 0
    private var failCardInsertSoundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setSavedLanguage()

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setOnCreate()

        sendLocalLogsToServer()
    }

    private fun sendLocalLogsToServer() {
        lifecycleScope.launch {
//          Send logs to server after Minute of app's working
            delay(60000)
            viewModel.sendLogsFromLocalToServer(this@MainActivity)
        }
    }

    private fun setOnCreate() {
        setSound()

        setupUSB()
//        setupBluetooth()

        registerBroadcastReciver()

        setWindowSettings()

        setObserver()
        clearDownloadedApk()
        showPresentation()
    }

    private fun setObserver() {
        observeNetworkConnectivity()

        observeGetGuardian()

        observeCheckIn()

        observeGetSchedules()

        observeCreateAppointment()
    }

    private fun observeNetworkConnectivity() {
        checkNetworkConnectivity.observe(this) { isConnected ->
            if (isConnected) {
                if (!isInternet) {
                    isInternet = true

                    viewModel.getClinicGuardian()

                    navHostFragment.visibility = View.VISIBLE
                    reconnectingLayout.visibility = View.GONE

                }
            } else {
                if (!isInternet) {
                    navHostFragment.visibility = View.GONE
                    reconnectingLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun observeGetGuardian() {
        viewModel.getGuardianStatus.observe(this) {
            dialog?.dismiss()

            dialog = when (it) {
                is MainViewModel.GetGuardianStatus.NotChecking -> {
                    if (it.response == null) {
                        null
                    } else {
                        if (it.response.success) {
                            null
                        } else {
                            val apiError = ApiError.initWith(it.response.code)

                            ErrorDialogFragment(
                                title = "",
                                message = apiError?.resStringID?.let { it1 -> getString(it1) }
                                    ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        Html.fromHtml(
                                            it.response.message,
                                            Html.FROM_HTML_MODE_COMPACT
                                        )
                                    } else {
                                        Html.fromHtml(it.response.message)
                                    }
                            )
                        }
                    }
                }
                MainViewModel.GetGuardianStatus.Checking -> {
                    CheckingDialogFragment()
                }
                is MainViewModel.GetGuardianStatus.Fail -> {
                    ErrorDialogFragment(
                        title = "",
                        message = it.throwable.message ?: "Unknown"
                    )
                }
            }

            dialog?.showNow(supportFragmentManager, null)
        }
    }

    private fun observeCheckIn() {
        viewModel.checkInStatus.observe(this) {
            dialog?.dismiss()

            dialog = when (it) {
                is MainViewModel.CheckInStatus.NotChecking -> {

                    val automaticAppointmentData = viewModel.automaticAppointmentSetting

                    if (it.response == null) {
                        null
                    } else {

                        if (it.response.code != 10010 || !automaticAppointmentData.isEnabled) {

                            soundPool.play(
                                if (it.response.success) {
                                    successSoundId
                                } else {
                                    if (it.response.code == 10013) {
                                        successSoundId
                                    } else {
                                        failSoundId
                                    }
                                },
                                1f,
                                1f,
                                0,
                                0,
                                1f
                            )

                        }

                        if (it.response.success) {

                            if (!it.isItManualInput) {
                                viewModel.makeSingleAutoAppointment(it) { createAppointmentResponse ->

                                    if (createAppointmentResponse.success && viewModel.queueingMachineSettingIsEnable) {

                                        val arrayDoctor: Array<String> = arrayOf(
                                            createAppointmentResponse.doctor
                                        )

                                        val arraySerialNumber: Array<Int> = arrayOf(
                                            createAppointmentResponse.serialNo
                                        )

                                        val arrayDivision: Array<String> = arrayOf(
                                            when (BuildConfig.BUILD_TYPE) {
                                                "ptch" -> createAppointmentResponse.doctor
                                                else -> createAppointmentResponse.division
                                            }
                                        )

                                        printPatient(
                                            divisions = arrayDivision,
                                            serialNumbers = arraySerialNumber,
                                            doctors = arrayDoctor
                                        )
                                    }
                                }
                            }

                            SuccessDialogFragment(
                                title = getString(R.string.success_to_check),
                                message = if (viewModel.queueingMachineSettingIsEnable) getString(R.string.success_to_check_message) else ""
                            )
                        } else if (it.response.code == 10013) {
                            SuccessDialogFragment(
                                title = getString(R.string.success_to_check),
                                message = ""
                            )
                        } else {
                            val apiError = ApiError.initWith(it.response.code)

                            when (apiError) {
                                ApiError.APPOINTMENT_NOT_FOUND -> {
                                    if (automaticAppointmentData.isEnabled) {
                                        if (it.isItManualInput) {
                                            ErrorDialogFragment(
                                                title = getString(R.string.fail_to_check),
                                                message = getString(apiError.resStringID),
                                                onActionButtonClicked = null
                                            )
                                        } else {
                                            if (automaticAppointmentData.mode == AutomaticAppointmentMode.MULTIPLE_MODE) {
                                                viewModel.getSchedule(patient = it.patient)
                                            } else {
                                                createAppointment(
                                                    schedule = GetScheduleResponse.ScheduleBean(
                                                        doctor = automaticAppointmentData.doctorId,
                                                        division = automaticAppointmentData.roomId
                                                    ),
                                                    patient = it.patient,
                                                    isAutomaticAppointment = true,
                                                    completion = { createAppointmentResponse ->
                                                        soundPool.play(
                                                            if (createAppointmentResponse.success) {
                                                                successSoundId
                                                            } else {
                                                                failSoundId
                                                            },
                                                            1f,
                                                            1f,
                                                            0,
                                                            0,
                                                            1f
                                                        )
                                                    }
                                                )
                                            }
                                            return@observe
                                        }
                                    } else {
                                        ErrorDialogFragment(
                                            title = getString(R.string.fail_to_check),
                                            message = getString(apiError.resStringID),
                                            onActionButtonClicked = null
                                        )
                                    }
                                }
                                else -> {
                                    ErrorDialogFragment(
                                        title = getString(R.string.fail_to_check),
                                        message = apiError?.resStringID?.let { it1 -> getString(it1) }
                                            ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                Html.fromHtml(
                                                    it.response._message.toCharSequence(this)
                                                        .toString(),
                                                    Html.FROM_HTML_MODE_COMPACT
                                                )
                                            } else {
                                                Html.fromHtml(
                                                    it.response._message.toCharSequence(
                                                        this
                                                    ).toString()
                                                )
                                            },
                                        onActionButtonClicked = null
                                    )
                                }
                            }
                        }
                    }
                }
                MainViewModel.CheckInStatus.Checking -> {
                    CheckingDialogFragment()
                }
                is MainViewModel.CheckInStatus.Fail -> {
                    ErrorDialogFragment(
                        title = getString(R.string.fail_to_check),
                        message = it.throwable.message ?: "Unknown"
                    )
                }
            }

            dialog?.showNow(supportFragmentManager, null)
        }
    }

    private fun observeGetSchedules() {
        viewModel.getSchedulesStatus.observe(this) {
            dialog?.dismiss()

            dialog = when (it) {
                is MainViewModel.GetSchedulesStatus.NotChecking -> {
                    if (it.response == null) {
                        null
                    } else {
                        if (it.response.success) {
                            ScheduleListDialogFragment(
                                schedules = it.response.schedules,
                                currentLanguage = viewModel.getLanguage()
                            ) { checkedSchedule ->
                                if (checkedSchedule != null) {
                                    createAppointment(
                                        isCheckIn = false,
                                        patient = it.patient,
                                        schedule = checkedSchedule
                                    )
                                } else {
                                    dialog?.dismiss()
                                    dialog = null
                                }
                            }
                        } else {
                            val apiError = ApiError.initWith(it.response.code)

                            ErrorDialogFragment(
                                title = "",
                                message = apiError?.resStringID?.let { it1 -> getString(it1) }
                                    ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        Html.fromHtml(
                                            it.response.message,
                                            Html.FROM_HTML_MODE_COMPACT
                                        )
                                    } else {
                                        Html.fromHtml(it.response.message)
                                    }
                            )
                        }
                    }
                }
                MainViewModel.GetSchedulesStatus.Checking -> {
                    CheckingDialogFragment()
                }
                is MainViewModel.GetSchedulesStatus.Fail -> {
                    ErrorDialogFragment(
                        title = "",
                        message = it.throwable.message ?: "Unknown"
                    )
                }
            }

            dialog?.showNow(supportFragmentManager, null)
        }
    }

    private fun observeCreateAppointment() {
        viewModel.createAppointmentStatus.observe(this) {
            dialog?.dismiss()

            dialog = when (it) {
                is MainViewModel.CreateAppointmentStatus.NotChecking -> {
                    if (it.response == null) {
                        null
                    } else {
                        if (it.response.success) {
                            SuccessDialogFragment(
                                title = getString(R.string.success_to_make_appointment),
                                message = getString(R.string.success_to_make_appointment_message)
                            )
                        } else {
                            val apiError = ApiError.initWith(it.response.code)

                            ErrorDialogFragment(
                                title = if (it.response.code == 10014) {
                                    getString(R.string.fail_to_make_appointment_10014)
                                } else {
                                    getString(R.string.fail_to_make_appointment)
                                },
                                message = apiError?.resStringID?.let { it1 -> getString(it1) }
                                    ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        Html.fromHtml(
                                            it.response.message,
                                            Html.FROM_HTML_MODE_COMPACT
                                        )
                                    } else {
                                        Html.fromHtml(it.response.message)
                                    }
                            )
                        }
                    }
                }
                MainViewModel.CreateAppointmentStatus.Checking -> {
                    CheckingDialogFragment()
                }
                is MainViewModel.CreateAppointmentStatus.Fail -> {
                    ErrorDialogFragment(
                        title = getString(R.string.fail_to_make_appointment),
                        message = it.throwable.message ?: "Unknown"
                    )
                }
            }

            dialog?.showNow(supportFragmentManager, null)
        }
    }

    private fun setWindowSettings() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val statusBarVisible = insets.isVisible(WindowInsetsCompat.Type.statusBars())

            if (!imeVisible && statusBarVisible) {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        delay(100)
                    }
                    hideSystemUI()
                }
            }

            insets
        }
    }

    private fun registerBroadcastReciver() {
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            reloadClinicDataReceiver,
            IntentFilter(SharedPreferencesRepo.MS_SERVER_DOMAIN).apply {
                addAction(SharedPreferencesRepo.ORG_ID)
            }
        )

        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            presentationReceiver,
            IntentFilter(SharedPreferencesRepo.QUEUEING_BOARD_SETTING)
        )

        val downloadFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(downloadReceiver, downloadFilter)
    }

    private fun setSound() {
        soundPool = SoundPool(
            6,
            AudioManager.STREAM_MUSIC,
            0
        )

        successSoundId = soundPool.load(assets.openFd("success.mp3"), 1)
        failSoundId = soundPool.load(assets.openFd("fail.mp3"), 1)
        failCardInsertSoundId = soundPool.load(assets.openFd("again.m4a"), 1)
    }

    fun checkInstallUnknownApkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return packageManager.canRequestPackageInstalls()

        return true
    }

    fun checkIsWriteExternalStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

        return true
    }

    fun getInstallUnknownApkPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:$packageName")
        )

        manageUnknownAppSourcesLauncher.launch(intent)
    }

    fun getWriteExternalStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1221
        )
    }

    private val manageUnknownAppSourcesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d("check---", "onRequestPermissionsResult: its work")
            } else {
                Log.d("check---", "onRequestPermissionsResult: its work but its not")
            }
        }

    private fun getDownloadedFileUri(downloadId: Long): String? {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = cursor.getInt(columnIndex)
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                val uriString = cursor.getString(uriIndex)
                cursor.close()
                return uriString
            }
        }
        cursor.close()
        return null
    }

    private fun installAPK(apkUri: Uri, context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val file = File(apkUri.path)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .setDataAndType(
                            uri,
                            "application/vnd.android.package-archive"
                        )
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (e: ActivityNotFoundException) {
            }
        } else {
            val installIntent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(
                    apkUri,
                    "application/vnd.android.package-archive"
                )
            finish()
            startActivity(installIntent)

        }
    }

    fun setLanguage(language: String) {
        val locale = checkForCountryInLanguageCode(language)

        val resources = resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    private fun checkForCountryInLanguageCode(
        languageCode: String,
    ): Locale {

        val langList = languageCode.split("-")

        val locale = if (langList.size > 1) {
            val countryPart = langList[1]
            val countryCode = if (countryPart[0] == 'r') countryPart.drop(1) else countryPart
            Locale(langList[0], countryCode)
        } else {
            Locale(langList[0])
        }
        return locale
    }

    private fun setSavedLanguage() {
        val language = viewModel.getLanguage()
        if (language.isBlank())
            setLanguage(language = BuildConfig.DEFAULT_LANGUAGE)
        else
            setLanguage(language = language)

    }


    private fun setupUSB() {
        val usbFilter = IntentFilter(USB_PERMISSION)
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)

        registerReceiver(
            detectUsbDeviceReceiver,
            usbFilter
        )

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        acsUsbDevice = AcsUsbDevice(
            usbManager,
            arrayOf(AcsUsbTWDecoder()),
        ).apply { callback = usbDeviceCallback }
        acsUsbDevice.supportedDevice?.also {
            connectUSBDevice(it)
        }

        ezUsbDevice = CastlesUsbDevice(this).apply { callback = usbDeviceCallback }
        ezUsbDevice.supportedDevice?.also {
            connectUSBDevice(it)
        }

        rfProDevice = RFProDevice(this).apply { callback = usbDeviceCallback }
        rfProDevice.supportedDevice?.also {
            connectUSBDevice(it)
        }

        usbPrinterService = BPT3XPrinterService.isSupported(usbManager)?.let {
            val service = BPT3XPrinterService(usbManager)
            connectUSBDevice(it)
            service
        } ?: EP360CPrintService.isSupported(usbManager)?.let {
            val service = EP360CPrintService(applicationContext)
            connectUSBDevice(it)
            service
        }
    }

    private fun connectUSBDevice(device: UsbDevice) {
        val mPermissionIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(USB_PERMISSION),
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) 0 else PendingIntent.FLAG_IMMUTABLE
        )

        usbManager.requestPermission(device, mPermissionIntent)
    }

//    /** print ticket */
//    private fun printPatient(division: String, serialNo: Int) {
//        val now = Date()
//        val formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
//
//        val commandList = arrayListOf(
//            PrinterBuffer.initializePrinter(),
//            PrinterBuffer.selectAlignment(PrinterBuffer.Alignment.CENTER),
//
//            PrinterBuffer.setLineSpacing(120),
//            PrinterBuffer.selectCharacterSize(PrinterBuffer.CharacterSize.XSMALL),
//            strToBytes(viewModel.clinicGuardian.value!!.name),
//            PrinterBuffer.printAndFeedLine(),
//
//            PrinterBuffer.setLineSpacing(160),
//            PrinterBuffer.selectCharacterSize(PrinterBuffer.CharacterSize.SMALL),
//            strToBytes(division),
//            PrinterBuffer.printAndFeedLine(),
//
//            PrinterBuffer.setLineSpacing(120),
//            PrinterBuffer.selectCharacterSize(PrinterBuffer.CharacterSize.XSMALL),
//            strToBytes(getString(R.string.print_serial_no)),
//            PrinterBuffer.printAndFeedLine(),
//
//            PrinterBuffer.setLineSpacing(160),
//            PrinterBuffer.selectCharacterSize(PrinterBuffer.CharacterSize.SMALL),
//            strToBytes(String.format("%04d", serialNo)),
//            PrinterBuffer.printAndFeedLine(),
//
//            PrinterBuffer.setLineSpacing(120),
//            PrinterBuffer.selectCharacterSize(PrinterBuffer.CharacterSize.XSMALL),
//            strToBytes(formatter.format(now)),
//            PrinterBuffer.printAndFeedLine(),
//
//            PrinterBuffer.setLineSpacing(120),
//            PrinterBuffer.selectCharacterSize(PrinterBuffer.CharacterSize.XSMALL),
//            strToBytes(getString(R.string.print_footer)),
//            PrinterBuffer.printAndFeedLine(),
//
//            PrinterBuffer.selectCutPagerModerAndCutPager(66, 1)
//        )
//
//        commandList.forEach { command ->
//            usbPrinterService.write(command)
//        }
//    }

    /** print ticket */
    private fun printPatient(
        divisions: Array<String>,
        serialNumbers: Array<Int>,
        doctors: Array<String>
    ) {

        require(divisions.size == serialNumbers.size && serialNumbers.size == doctors.size) {
            "Arrays must have the same size"
        }

        viewModel.eventAppPrintsATicket(
            divisions = divisions,
            serialNumbers = serialNumbers,
            doctors = doctors
        )

        val now = Date()
        val formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        val queueingMachineSettings = viewModel.queueingMachineSettings

        if (queueingMachineSettings.isOneTicket) {

            val headerCommand = getHeaderCommand(
                isShowOrganization = queueingMachineSettings.organization,
                textSize = queueingMachineSettings.textSize
            )

            val middleCommand =
                getMiddleCommand(
                    doctors = doctors,
                    divisions = divisions,
                    serialNumbers = serialNumbers,
                    queueingMachineSettingModel = queueingMachineSettings
                )

            val footerCommand = getFooterCommand(
                isShowTime = queueingMachineSettings.time,
                formatter = formatter,
                now = now,
                textSize = queueingMachineSettings.textSize
            )

            val commandList: ArrayList<ByteArray> = ArrayList()

            commandList.addAll(headerCommand)
            commandList.addAll(middleCommand)
            commandList.addAll(footerCommand)

            commandList.forEach { command ->
                usbPrinterService?.write(command)
            }
        } else {
            divisions.zipWith(serialNumbers, doctors).forEach { (division, serialNo, doctor) ->

                val doctorArray = arrayOf(doctor)
                val divisionArray = arrayOf(division)
                val serialNoArray = arrayOf(serialNo)

                val headerCommand = getHeaderCommand(
                    isShowOrganization = queueingMachineSettings.organization,
                    textSize = queueingMachineSettings.textSize
                )
                val middleCommand =
                    getMiddleCommand(
                        doctors = doctorArray,
                        divisions = divisionArray,
                        serialNumbers = serialNoArray,
                        queueingMachineSettingModel = queueingMachineSettings
                    )
                val footerCommand = getFooterCommand(
                    isShowTime = queueingMachineSettings.time,
                    formatter = formatter,
                    now = now,
                    textSize = queueingMachineSettings.textSize
                )

                val commandList: ArrayList<ByteArray> = ArrayList()

                commandList.addAll(headerCommand)
                commandList.addAll(middleCommand)
                commandList.addAll(footerCommand)

                commandList.forEach { command ->
                    usbPrinterService?.write(command)
                }
            }
        }
    }

    private fun getHeaderCommand(
        isShowOrganization: Boolean,
        textSize: QueueingMachineSettingModel.MillimeterSize
    ): ArrayList<ByteArray> {
        return when (usbPrinterService) {
            is BPT3XPrinterService -> {
                val headerCommand = arrayListOf(
                    PrinterBuffer.initializePrinter(),
                    PrinterBuffer.selectAlignment(PrinterBuffer.Alignment.CENTER),
                )

                val clinicName = viewModel.clinicName

                if (isShowOrganization && clinicName.isNotBlank()) {
                    headerCommand.addAll(
                        arrayListOf(
                            PrinterBuffer.setLineSpacing(120),
                            PrinterBuffer.selectCharacterSize(
                                setTextSizeForSmallText(textSize)
                            ),
                            strToBytes(clinicName),
                            PrinterBuffer.printAndFeedLine(),
                        )
                    )
                }
                headerCommand
            }
            is EP360CPrintService -> {
                val headerCommand = arrayListOf(
                    EP360CPrinterBuffer.initializePrinter(),
                    EP360CPrinterBuffer.align(EP360CPrinterBuffer.Alignment.CENTER),
                )

                val clinicName = viewModel.clinicName

                if (isShowOrganization && clinicName.isNotBlank()) {
                    headerCommand.addAll(
                        arrayListOf(
                            EP360CPrinterBuffer.feedLine(),
                            EP360CPrinterBuffer.textOut(
                                clinicName,
                                textWidth = 3,
                                textHeight = 2,
                                fontStyle = EP360CPrinterBuffer.TextStyle.BOLD
                            ),
                            EP360CPrinterBuffer.feedLine(),
                            EP360CPrinterBuffer.feedLine(),
                        )
                    )
                }
                headerCommand
            }
            else -> TODO()
        }
    }

    private fun getMiddleCommand(
        doctors: Array<String>,
        divisions: Array<String>,
        serialNumbers: Array<Int>,
        queueingMachineSettingModel: QueueingMachineSettingModel
    ): ArrayList<ByteArray> {
        val middleCommand: ArrayList<ByteArray> = ArrayList()

        val textSize = queueingMachineSettingModel.textSize

        divisions.zipWith(serialNumbers, doctors).forEach { (division, serialNo, doctor) ->

            when (usbPrinterService) {
                is BPT3XPrinterService -> {
                    if (queueingMachineSettingModel.doctor) {
                        middleCommand.addAll(
                            arrayListOf(
                                PrinterBuffer.setLineSpacing(160),
                                PrinterBuffer.selectCharacterSize(setTextSizeForBigText(textSize)),
                                strToBytes(doctor),
                                PrinterBuffer.printAndFeedLine(),
                            )
                        )
                    }

                    if (queueingMachineSettingModel.dept) {
                        middleCommand.addAll(
                            arrayListOf(
                                PrinterBuffer.setLineSpacing(160),
                                PrinterBuffer.selectCharacterSize(setTextSizeForBigText(textSize)),
                                strToBytes(division),
                                PrinterBuffer.printAndFeedLine(),
                            )
                        )
                    }

                    val innerList = arrayListOf(
                        PrinterBuffer.setLineSpacing(120),
                        PrinterBuffer.selectCharacterSize(setTextSizeForSmallText(textSize)),
                        strToBytes(getString(R.string.print_serial_no)),
                        PrinterBuffer.printAndFeedLine(),

                        PrinterBuffer.setLineSpacing(160),
                        PrinterBuffer.selectCharacterSize(setTextSizeForBigText(textSize)),
                        strToBytes(String.format("%04d", serialNo)),
                        PrinterBuffer.printAndFeedLine(),
                    )

                    middleCommand.addAll(innerList)
                }
                is EP360CPrintService -> {
                    if (queueingMachineSettingModel.doctor) {
                        middleCommand.addAll(
                            arrayListOf(
                                EP360CPrinterBuffer.textOut(
                                    doctor,
                                    textWidth = 7,
                                    textHeight = 4,
                                    fontStyle = EP360CPrinterBuffer.TextStyle.BOLD
                                ),
                                EP360CPrinterBuffer.feedLine(),
                                EP360CPrinterBuffer.feedLine(),
                            )
                        )
                    }

                    if (queueingMachineSettingModel.dept) {
                        middleCommand.addAll(
                            arrayListOf(
                                EP360CPrinterBuffer.textOut(
                                    division,
                                    textWidth = 7,
                                    textHeight = 4,
                                    fontStyle = EP360CPrinterBuffer.TextStyle.BOLD
                                ),
                                EP360CPrinterBuffer.feedLine(),
                                EP360CPrinterBuffer.feedLine(),
                            )
                        )
                    }

                    val innerList = arrayListOf(
                        EP360CPrinterBuffer.textOut(
                            getString(R.string.print_serial_no),
                            textWidth = 3,
                            textHeight = 2,
                            fontStyle = EP360CPrinterBuffer.TextStyle.BOLD
                        ),
                        EP360CPrinterBuffer.feedLine(),
                        EP360CPrinterBuffer.feedLine(),

                        EP360CPrinterBuffer.textOut(
                            String.format("%04d", serialNo),
                            textWidth = 7,
                            textHeight = 4,
                            fontStyle = EP360CPrinterBuffer.TextStyle.BOLD
                        ),
                        EP360CPrinterBuffer.feedLine(),
                        EP360CPrinterBuffer.feedLine(),
                    )

                    middleCommand.addAll(innerList)
                }
                else -> TODO()
            }
        }
        return middleCommand
    }

    private fun getFooterCommand(
        isShowTime: Boolean,
        formatter: DateFormat,
        now: Date,
        textSize: QueueingMachineSettingModel.MillimeterSize
    ): ArrayList<ByteArray> {
        val footerCommand: ArrayList<ByteArray> = ArrayList()

        when (usbPrinterService) {
            is BPT3XPrinterService -> {
                if (isShowTime) {
                    footerCommand.addAll(
                        arrayListOf(
                            PrinterBuffer.setLineSpacing(120),
                            PrinterBuffer.selectCharacterSize(setTextSizeForSmallText(textSize)),
                            strToBytes(formatter.format(now)),
                            PrinterBuffer.printAndFeedLine(),
                        )
                    )
                }

                footerCommand.addAll(
                    arrayListOf(
                        PrinterBuffer.setLineSpacing(120),
                        PrinterBuffer.selectCharacterSize(setTextSizeForSmallText(textSize)),
                        strToBytes(getString(R.string.print_footer)),
                        PrinterBuffer.printAndFeedLine(),

                        PrinterBuffer.selectCutPagerModerAndCutPager(66, 1)
                    )
                )
            }
            is EP360CPrintService -> {
                if (isShowTime) {
                    footerCommand.addAll(
                        arrayListOf(
                            EP360CPrinterBuffer.textOut(
                                formatter.format(now),
                                textWidth = 1,
                                textHeight = 0
                            ),
                            EP360CPrinterBuffer.feedLine(),
                        )
                    )
                }

                footerCommand.addAll(
                    arrayListOf(
                        EP360CPrinterBuffer.textOut(
                            getString(R.string.print_footer),
                            textWidth = 1,
                            textHeight = 0
                        ),
                        EP360CPrinterBuffer.feedLine(),

                        EP360CPrinterBuffer.feedLine(),
                        EP360CPrinterBuffer.feedLine(),
                        EP360CPrinterBuffer.feedLine(),
                        EP360CPrinterBuffer.halfCutPaper()
                    )
                )
            }
            else -> TODO()
        }

        return footerCommand
    }


    private fun setTextSizeForSmallText(textSize: QueueingMachineSettingModel.MillimeterSize) =
        when (textSize) {
            QueueingMachineSettingModel.MillimeterSize.FIFTY_SEVEN_MILLIMETERS ->
                PrinterBuffer.CharacterSize.XXSMALL
            QueueingMachineSettingModel.MillimeterSize.SEVENTY_SIX_MILLIMETERS ->
                PrinterBuffer.CharacterSize.XSMALL
        }

    private fun setTextSizeForBigText(textSize: QueueingMachineSettingModel.MillimeterSize) =
        when (textSize) {
            QueueingMachineSettingModel.MillimeterSize.FIFTY_SEVEN_MILLIMETERS ->
                PrinterBuffer.CharacterSize.XSMALL
            QueueingMachineSettingModel.MillimeterSize.SEVENTY_SIX_MILLIMETERS ->
                PrinterBuffer.CharacterSize.SMALL
        }


    //        val bluetoothStateFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)


    /** 將字串用萬國編碼轉成ByteArray防止中文亂碼 */
    private fun strToBytes(str: String): ByteArray = str.toByteArray(charset("big5"))

//    private fun setupBluetooth() {
//        registerReceiver(
//            detectBluetoothStateReceiver,
//            bluetoothStateFilter
//        )
//
//        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
//        registerReceiver(detectBluetoothDeviceReceiver, filter)
//
//        requestBluetoothPermissions()
//
//        printService = BluetoothPrinterService(
//            this,
//            object : PrinterServiceDelegate {
//                override fun onStateChanged(state: PrinterServiceDelegate.State) {
//                    when (state) {
//                        PrinterServiceDelegate.State.NONE -> {
//                            startScan()
//                        }
//                        PrinterServiceDelegate.State.CONNECTING -> {
//                            stopScan()
//                        }
//                        PrinterServiceDelegate.State.CONNECTED -> {
//
//                        }
//                    }
//                }
//            }
//        )
//    }
//
//    private fun requestBluetoothPermissions() {
//        val permissions = arrayOf(
//            android.Manifest.permission.BLUETOOTH,
//            android.Manifest.permission.BLUETOOTH_ADMIN,
//            android.Manifest.permission.ACCESS_FINE_LOCATION,
//            android.Manifest.permission.ACCESS_COARSE_LOCATION
//        )
//
//        bleForResult.launch(permissions)
//    }
//
//    private fun enableBluetoothService() {
//        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//
//        if (bluetoothAdapter.isEnabled) {
//            startScan()
//        } else {
////            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
////            bleForResult.launch(enableBtIntent)
//            bluetoothAdapter.enable()
//        }
//    }
//
//    var timeOutJob: Job? = null
//
//    /** 開始掃描藍牙設備 */
//    private fun startScan() {
//        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//
//        if (bluetoothAdapter.isDiscovering || printService.state != PrinterServiceDelegate.State.NONE) return
//        Timber.d("startDiscovery")
//
//        if (bluetoothAdapter.isEnabled) {
//            timeOutJob?.cancel()
//            timeOutJob = lifecycleScope.launch {
//                withContext(Dispatchers.IO) {
//                    delay(TimeUnit.SECONDS.toMillis(SCAN_TIME_OUT))
//                }
//
//                if (bluetoothAdapter.isEnabled && printService.state != PrinterServiceDelegate.State.CONNECTED) {
//                    bluetoothAdapter.disable()
//                }
//            }
//
//            bluetoothAdapter.startDiscovery()
//        }
//    }
//
//    /** 停止掃描藍牙設備 */
//    private fun stopScan() {
//        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//
//        if (!bluetoothAdapter.isDiscovering) return
//        Timber.d("cancelDiscovery")
//
//        if (bluetoothAdapter.isEnabled) {
//            bluetoothAdapter.cancelDiscovery()
//        }
//    }

    /** get patient appointment */
    fun getPatients(
        nationalId: String,
        name: String = "",
        birth: String = "",
        isItManualInput: Boolean = false,
        completion: (() -> Unit)? = null
    ) {
        if (viewModel.queueingMachineSettingIsEnable && usbPrinterService?.isConnected != true) {
            // 若有開啟取號功能，則必須要有連線取票機才會去報到
            Snackbar.make(
                binding.root,
                getString(R.string.printer_not_connect),
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        viewModel.getPatients(
            patient = CreateAppointmentRequest.Patient(
                nationalId = nationalId,
                name = name.ifBlank { nationalId },
                birthday = birth
            ),
            isItManualInput = isItManualInput
        ) {
            completion?.let { it1 -> it1() }

            if (it.success && viewModel.queueingMachineSettingIsEnable) {

                val arrayDoctor = it.patients.map { patient -> patient.doctor }.toTypedArray()
                val arraySerialNumber =
                    it.patients.map { patient -> patient.serialNo }.toTypedArray()

                val arrayDivision =
                    it.patients.map { patient ->
                        when (BuildConfig.BUILD_TYPE) {
                            "ptch" -> patient.doctor
                            else -> patient.division
                        }
                    }.toTypedArray()

                printPatient(
                    divisions = arrayDivision,
                    serialNumbers = arraySerialNumber,
                    doctors = arrayDoctor
                )
            }
        }
    }

    /** Do not have NHI Card, manual check in */
    private fun createAppointment(
        isCheckIn: Boolean = true,
        schedule: GetScheduleResponse.ScheduleBean,
        patient: CreateAppointmentRequest.Patient? = null,
        isAutomaticAppointment: Boolean = false,
        completion: ((CreateAppointmentResponse) -> Unit)? = null
    ) {
        // if app support print ticket, check ticket machine connection
        if (viewModel.queueingMachineSettingIsEnable && usbPrinterService?.isConnected != true) {
            Snackbar.make(
                binding.root,
                getString(R.string.printer_not_connect),
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        viewModel.createAppointment(
            schedule = schedule,
            patient = patient,
            isAutomaticAppointment = isAutomaticAppointment,
        ) { createAppointmentResponse ->
            completion?.invoke(createAppointmentResponse)

            if (createAppointmentResponse.success && viewModel.queueingMachineSettingIsEnable) {

                val arrayDoctor: Array<String> = arrayOf(
                    createAppointmentResponse.doctor
                )

                val arraySerialNumber: Array<Int> = arrayOf(
                    createAppointmentResponse.serialNo
                )

                val arrayDivision: Array<String> = arrayOf(
                    when (BuildConfig.BUILD_TYPE) {
                        "ptch" -> createAppointmentResponse.doctor
                        else -> createAppointmentResponse.division
                    }
                )


                printPatient(
                    divisions = arrayDivision,
                    serialNumbers = arraySerialNumber,
                    doctors = arrayDoctor
                )
            }
        }
    }

    /** manual check in */
    fun createFakeAppointment(schedule: GetScheduleResponse.ScheduleBean) {
        val serialNo = viewModel.checkInSerialNo

        createAppointment(
            isCheckIn = false,
            schedule = schedule,
            patient = CreateAppointmentRequest.Patient(
                name = "手動取號",
                nationalId = "Fake${String.format("%06d", serialNo)}"
            )
        ) { createAppointmentResponse ->

            viewModel.checkInSerialNo = if (serialNo >= 999999) {
                0
            } else {
                serialNo + 1
            }
        }
    }

    /** check in with virtual NHI card */
    fun checkInWithVirtualCard() {
        dialog?.dismiss()

        dialog = ErrorDialogFragment(
            title = getString(R.string.coming_soon),
            message = ""
        )

        dialog?.showNow(supportFragmentManager, null)
    }

    /** find second display screen, if exist, show queue board on second display screen */
    private fun showPresentation() {
        val mediaRouter = getSystemService(ComponentActivity.MEDIA_ROUTER_SERVICE) as MediaRouter
        val presentationDisplay =
            mediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO).presentationDisplay
                ?: return

        try {
            val cls =
                Class.forName("com.advmeds.cliniccheckinapp.ui.presentations.WebPresentation").kotlin
            presentation =
                cls.primaryConstructor?.call(this, presentationDisplay) as? Presentation

            if (viewModel.queueingBoardSettingIsEnable)
                presentation?.show()
        } catch (ignored: ClassNotFoundException) {

        }
    }

    private fun hideSystemUI() {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView) ?: return
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // hide soft keyboard when touching outside
        val v = currentFocus

        if (v != null &&
            (ev?.action == MotionEvent.ACTION_DOWN) &&
            v is EditText &&
            !v::class.java.simpleName.startsWith("android.webkit")
        ) {
            val sourceCoordinates = IntArray(2)

            v.getLocationOnScreen(sourceCoordinates)

            val x = ev.rawX + v.left - sourceCoordinates[0]
            val y = ev.rawY + v.top - sourceCoordinates[1]

            if (x < v.left || x > v.right || y < v.top || y > v.bottom) {
                v.clearFocus()

                WindowCompat.getInsetsController(window, window.decorView)?.run {
                    // Hide both the status bar and the navigation bar
                    hide(WindowInsetsCompat.Type.ime())
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    private fun clearDownloadedApk(): Int {
        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val query = DownloadManager.Query()
        val cursor = manager.query(query)

        if (!cursor.moveToFirst()) {
            return 0
        }

        var deletedCount = 0

        do {
            val fileTitleColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
            val fileTitle = cursor.getString(fileTitleColumnIndex)

            if (fileTitle == DownloadController.getFileName()) {
                val idColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID)
                val id = cursor.getLong(idColumnIndex)

                manager.remove(id)

                deletedCount++
            }

        } while (cursor.moveToNext())

        return deletedCount
    }


    override fun onDestroy() {
        super.onDestroy()

//        printService.disconnect()

//        stopScan()

        acsUsbDevice.disconnect()
        ezUsbDevice.disconnect()
        rfProDevice.disconnect()
        usbPrinterService?.disconnect()
        usbPrinterService = null

        try {
            unregisterReceiver(detectUsbDeviceReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(reloadClinicDataReceiver)
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(presentationReceiver)
        unregisterReceiver(downloadReceiver)

//        try {
//            unregisterReceiver(detectBluetoothStateReceiver)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        try {
//            unregisterReceiver(detectBluetoothDeviceReceiver)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }
}