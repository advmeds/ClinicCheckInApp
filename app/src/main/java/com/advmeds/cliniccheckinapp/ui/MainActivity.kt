package com.advmeds.cliniccheckinapp.ui

import android.app.PendingIntent
import android.app.Presentation
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.media.AudioManager
import android.media.MediaRouter
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.MotionEvent
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.advmeds.cardreadermodule.AcsResponseModel
import com.advmeds.cardreadermodule.UsbDeviceCallback
import com.advmeds.cardreadermodule.acs.usb.AcsUsbDevice
import com.advmeds.cardreadermodule.acs.usb.decoder.AcsUsbTWDecoder
import com.advmeds.cardreadermodule.castles.CastlesUsbDevice
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
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueueingMachineSettingModel
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo
import com.advmeds.cliniccheckinapp.utils.zipWith
import com.advmeds.printerlib.usb.BPT3XPrinterService
import com.advmeds.printerlib.usb.UsbPrinterService
import com.advmeds.printerlib.utils.PrinterBuffer
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.*
import kotlin.reflect.full.primaryConstructor

class MainActivity : AppCompatActivity() {
    companion object {
        private const val USB_PERMISSION = "${BuildConfig.APPLICATION_ID}.USB_PERMISSION"
//        private const val SCAN_TIME_OUT: Long = 15
    }

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    var dialog: AppCompatDialogFragment? = null
    private var presentation: Presentation? = null

    private val detectUsbDeviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val usbDevice = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE) ?: return

            when (intent.action) {
                USB_PERMISSION -> {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        // user choose YES for your previously popup window asking for grant permission for this usb device
                        when (usbDevice.productId) {
                            acsUsbDevice.supportedDevice?.productId -> {
                                acsUsbDevice.connectDevice(usbDevice)
                            }
                            ezUsbDevice.supportedDevice?.productId -> {
                                ezUsbDevice.connectDevice(usbDevice)
                            }
                            usbPrinterService.supportedDevice?.productId -> {
                                if (viewModel.queueingMachineSettingIsEnable) {
                                    try {
                                        usbPrinterService.connectDevice(usbDevice)
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
                        }
                    } else {
                        // user choose NO for your previously popup window asking for grant permission for this usb device
                    }
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    connectUSBDevice(usbDevice)
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    when (usbDevice.productId) {
                        acsUsbDevice.connectedDevice?.productId -> {
                            acsUsbDevice.disconnect()
                        }
                        ezUsbDevice.connectedDevice?.productId -> {
                            ezUsbDevice.disconnect()
                        }
                        usbPrinterService.connectedDevice?.productId -> {
                            usbPrinterService.disconnect()
                        }
                    }
                }
            }
        }
    }

    private lateinit var usbManager: UsbManager

    private lateinit var acsUsbDevice: AcsUsbDevice

    private lateinit var ezUsbDevice: CastlesUsbDevice

    private val usbDeviceCallback = object : UsbDeviceCallback {
        override fun onCardPresent() {

        }

        override fun onCardAbsent() {
//            viewModel.cancelJobOnCardAbsent()
//            dialog?.dismiss()
//            dialog = null

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
                it.message?.let { it1 ->
                    Snackbar.make(binding.root, it1, Snackbar.LENGTH_LONG).show()
                }
            }
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

    lateinit var usbPrinterService: UsbPrinterService

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

    private lateinit var soundPool: SoundPool
    private var successSoundId: Int = 0
    private var failSoundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        setSavedLanguage()

        super.onCreate(savedInstanceState)

        soundPool = SoundPool(
            6,
            AudioManager.STREAM_MUSIC,
            0
        )

        successSoundId = soundPool.load(assets.openFd("success.mp3"), 1)
        failSoundId = soundPool.load(assets.openFd("fail.mp3"), 1)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            reloadClinicDataReceiver,
            IntentFilter(SharedPreferencesRepo.MS_SERVER_DOMAIN).apply {
                addAction(SharedPreferencesRepo.ORG_ID)
            }
        )

        LocalBroadcastManager.getInstance(this).registerReceiver(
            presentationReceiver,
            IntentFilter(SharedPreferencesRepo.QUEUEING_BOARD_SETTING)
        )

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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUSB()
//        setupBluetooth()

        viewModel.getClinicGuardian()

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

        viewModel.checkInStatus.observe(this) {
            dialog?.dismiss()

            dialog = when (it) {
                is MainViewModel.CheckInStatus.NotChecking -> {
                    if (it.response == null) {
                        null
                    } else {
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

                        if (it.response.success) {
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

                            ErrorDialogFragment(
                                title = getString(R.string.fail_to_check),
                                message = apiError?.resStringID?.let { it1 -> getString(it1) }
                                    ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        Html.fromHtml(
                                            it.response.message,
                                            Html.FROM_HTML_MODE_COMPACT
                                        )
                                    } else {
                                        Html.fromHtml(it.response.message)
                                    },
                                onActionButtonClicked = null
                            )

//                            val apiError = ApiError.initWith(it.response.code)
//
//                            ErrorDialogFragment(
//                                title = if (BuildConfig.PRINT_ENABLED && apiError == ApiError.APPOINTMENT_NOT_FOUND) {
//                                    getString(R.string.schedule_not_found)
//                                } else {
//                                    getString(R.string.fail_to_check)
//                                },
//                                message = if (BuildConfig.PRINT_ENABLED && apiError == ApiError.APPOINTMENT_NOT_FOUND) {
//                                    getString(R.string.make_appointment_now)
//                                } else {
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                        Html.fromHtml(
//                                            it.response.message,
//                                            Html.FROM_HTML_MODE_COMPACT
//                                        )
//                                    } else {
//                                        Html.fromHtml(it.response.message)
//                                    }
//                                },
//                                onActionButtonClicked = if (BuildConfig.PRINT_ENABLED && apiError == ApiError.APPOINTMENT_NOT_FOUND){
//                                    { isCancelled ->
//                                        if (!isCancelled) {
//                                            viewModel.getSchedule()
//                                        } else {
//                                            dialog?.dismiss()
//                                            dialog = null
//                                        }
//                                    }
//                                } else {
//                                    null
//                                }
//                            )
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

        viewModel.getSchedulesStatus.observe(this) {
            dialog?.dismiss()

            dialog = when (it) {
                is MainViewModel.GetSchedulesStatus.NotChecking -> {
                    if (it.response == null) {
                        null
                    } else {
                        if (it.response.success) {
                            ScheduleListDialogFragment(
                                schedules = it.response.schedules
                            ) { checkedSchedule ->
                                if (checkedSchedule != null) {
                                    createAppointment(checkedSchedule)
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

        showPresentation()
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


        usbPrinterService = BPT3XPrinterService(usbManager)
        if (viewModel.queueingMachineSettingIsEnable) {
            usbPrinterService.supportedDevice?.also {
                connectUSBDevice(it)
            }
        }
    }

    private fun connectUSBDevice(device: UsbDevice) {
        val mPermissionIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(USB_PERMISSION),
            0
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

        val now = Date()
        val formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        val queueingMachineSettings = viewModel.queueingMachineSettings

        val headerCommand = getHeaderCommand(queueingMachineSettings.organization)
        val middleCommand =
            getMiddleCommand(doctors, divisions, serialNumbers, queueingMachineSettings)
        val footerCommand = getFooterCommand(queueingMachineSettings.time, formatter, now)

        val commandList: ArrayList<ByteArray> = ArrayList()

        commandList.addAll(headerCommand)
        commandList.addAll(middleCommand)
        commandList.addAll(footerCommand)

        commandList.forEach { command ->
            usbPrinterService.write(command)
        }
    }

    private fun getHeaderCommand(isShowOrganization: Boolean): ArrayList<ByteArray> {
        val headerCommand = arrayListOf(
            PrinterBuffer.initializePrinter(),
            PrinterBuffer.selectAlignment(PrinterBuffer.Alignment.CENTER),
        )

        if (isShowOrganization) {
            headerCommand.addAll(
                arrayListOf(
                    PrinterBuffer.setLineSpacing(120),
                    PrinterBuffer.selectCharacterSize(PrinterBuffer.CharacterSize.XSMALL),
                    strToBytes(viewModel.clinicGuardian.value!!.name),
                    PrinterBuffer.printAndFeedLine(),
                )
            )
        }
        return headerCommand
    }

    private fun getMiddleCommand(
        doctors: Array<String>,
        divisions: Array<String>,
        serialNumbers: Array<Int>,
        queueingMachineSettingModel: QueueingMachineSettingModel
    ): ArrayList<ByteArray> {
        val middleCommand: ArrayList<ByteArray> = ArrayList()

        divisions.zipWith(serialNumbers, doctors).forEach { (division, serialNo, doctor) ->

            if (queueingMachineSettingModel.doctor) {
                middleCommand.addAll(
                    arrayListOf(
                        PrinterBuffer.setLineSpacing(160),
                        PrinterBuffer.selectCharacterSize(PrinterBuffer.CharacterSize.SMALL),
                        strToBytes(doctor),
                        PrinterBuffer.printAndFeedLine(),
                    )
                )
            }

            if (queueingMachineSettingModel.dept) {
                middleCommand.addAll(
                    arrayListOf(
                        PrinterBuffer.setLineSpacing(160),
                        PrinterBuffer.selectCharacterSize(PrinterBuffer.CharacterSize.SMALL),
                        strToBytes(division),
                        PrinterBuffer.printAndFeedLine(),
                    )
                )
            }

            val innerList = arrayListOf(

                PrinterBuffer.setLineSpacing(120),
                PrinterBuffer.selectCharacterSize(PrinterBuffer.CharacterSize.XSMALL),
                strToBytes(getString(R.string.print_serial_no)),
                PrinterBuffer.printAndFeedLine(),

                PrinterBuffer.setLineSpacing(160),
                PrinterBuffer.selectCharacterSize(PrinterBuffer.CharacterSize.SMALL),
                strToBytes(String.format("%04d", serialNo)),
                PrinterBuffer.printAndFeedLine(),
            )

            middleCommand.addAll(innerList)

        }
        return middleCommand
    }

    private fun getFooterCommand(
        isShowTime: Boolean,
        formatter: DateFormat,
        now: Date
    ): java.util.ArrayList<ByteArray> {
        val footerCommand = if (isShowTime) {
            arrayListOf(
                PrinterBuffer.setLineSpacing(120),
                PrinterBuffer.selectCharacterSize(PrinterBuffer.CharacterSize.XSMALL),
                strToBytes(formatter.format(now)),
                PrinterBuffer.printAndFeedLine(),
            )
        } else {
            ArrayList()
        }

        footerCommand.addAll(
            arrayListOf(
                PrinterBuffer.setLineSpacing(120),
                PrinterBuffer.selectCharacterSize(PrinterBuffer.CharacterSize.XSMALL),
                strToBytes(getString(R.string.print_footer)),
                PrinterBuffer.printAndFeedLine(),

                PrinterBuffer.selectCutPagerModerAndCutPager(66, 1)
            )
        )
        return footerCommand
    }

    /** 將字串用萬國編碼轉成ByteArray防止中文亂碼 */
    private fun strToBytes(str: String): ByteArray = str.toByteArray(charset("big5"))

//    private fun setupBluetooth() {
//        val bluetoothStateFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
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
        completion: (() -> Unit)? = null
    ) {
        if (viewModel.queueingMachineSettingIsEnable && !usbPrinterService.isConnected) {
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
            )
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
        schedule: GetScheduleResponse.ScheduleBean,
        patient: CreateAppointmentRequest.Patient? = null,
        completion: ((CreateAppointmentResponse) -> Unit)? = null
    ) {
        // if app support print ticket, check ticket machine connection
        if (viewModel.queueingMachineSettingIsEnable && !usbPrinterService.isConnected) {
            Snackbar.make(
                binding.root,
                getString(R.string.printer_not_connect),
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        viewModel.createAppointment(
            schedule = schedule,
            patient = patient
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
            schedule = schedule,
            patient = CreateAppointmentRequest.Patient(
                name = "手動取號",
                nationalId = "Fake${String.format("%06d", serialNo)}"
            )
        ) { createAppointmentResponse ->
            if (createAppointmentResponse.success) {
                viewModel.checkInSerialNo = if (serialNo >= 999999) {
                    0
                } else {
                    serialNo + 1
                }
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

    override fun onDestroy() {
        super.onDestroy()

//        printService.disconnect()

//        stopScan()

        acsUsbDevice.disconnect()
        ezUsbDevice.disconnect()
        usbPrinterService.disconnect()

        try {
            unregisterReceiver(detectUsbDeviceReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(reloadClinicDataReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(presentationReceiver)

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