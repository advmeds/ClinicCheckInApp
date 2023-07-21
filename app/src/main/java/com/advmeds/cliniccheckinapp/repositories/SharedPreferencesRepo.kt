package com.advmeds.cliniccheckinapp.repositories

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.advmeds.cliniccheckinapp.BuildConfig
import com.advmeds.cliniccheckinapp.dialog.EditCheckInItemDialog
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.AutomaticAppointmentSettingModel
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueueingMachineSettingModel
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueuingBoardSettingModel
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SharedPreferencesRepo(
    context: Context
) {
    private val sharedPreferences = context.getSharedPreferences(
        BuildConfig.APPLICATION_ID,
        Context.MODE_PRIVATE
    )

    private val localBroadcastManager = LocalBroadcastManager.getInstance(context)

    companion object {
        /** mScheduler Server domain key of SharedPreferences */
        const val MS_SERVER_DOMAIN = "ms_server_domain"

        /** clinic id key of SharedPreferences */
        const val ORG_ID = "org_id"

        /** specify clinic doctor id key of SharedPreferences */
        const val DOCTORS = "doctors"

        /** specify clinic room id key of SharedPreferences */
        const val ROOMS = "rooms"

        /** queue board url key of SharedPreferences */
        const val CLINIC_PANEL_MODE_IS_ENABLED = "clinic_panel_mode_is_enabled"
        const val CLINIC_PANEL_MODE = "clinic_panel_mode"

        /** clinic LOGO url key of SharedPreferences */
        const val LOGO_URL = "logo_url"

        /** manual check-in serial number key of SharedPreferences */
        const val CHECK_IN_SERIAL_NO = "check_in_serial_no"

        /** patient national id pattern key of SharedPreferences */
        const val FORMAT_CHECKED_LIST = "format_checked_list"

        /** manual check-in item key of SharedPreferences */
        const val CHECK_IN_ITEM_LIST = "check_in_item_list"

        /** 從SharedPreferences取得『Department ID』的KEY */
        const val DEPT_ID = "dept_id"

        /** SharedPreferences [queue board setting param] KEY */
        const val QUEUEING_BOARD_SETTING = "queueing_board"

        /** SharedPreferences [queue machine setting params] KEY */
        const val QUEUEING_MACHINE_SETTING_IS_ENABLE = "queueing_machine_setting_is_enable"
        const val QUEUEING_MACHINE_SETTING_ORGANIZATION = "queueing_machine_setting_organization"
        const val QUEUEING_MACHINE_SETTING_DOCTOR = "queueing_machine_setting_organization_doctor"
        const val QUEUEING_MACHINE_SETTING_DEPT = "queueing_machine_setting_organization_dept"
        const val QUEUEING_MACHINE_SETTING_TIME = "queueing_machine_setting_organization_time"
        const val QUEUEING_MACHINE_SETTING_IS_ONE_TICKET = "queueing_machine_setting_organization_is_one_ticket"

        /** SharedPreferences [automatic appointment setting params] KEY */
        const val AUTOMATIC_APPOINTMENT_SETTING_IS_ENABLE =
            "automatic_appointment_setting_is_enable"
        const val AUTOMATIC_APPOINTMENT_SETTING_DOCTOR = "automatic_appointment_setting_doctor"
        const val AUTOMATIC_APPOINTMENT_SETTING_ROOM = "automatic_appointment_setting_room"
        const val AUTOMATIC_APPOINTMENT_SETTING_AUTO_CHECK_IN = "automatic_appointment_setting_auto_check_in"

        /** SharedPreferences『Language』KEY */
        const val LANGUAGE_KEY = "language"

        /** SharedPreferences『PASSWORD』KEY */
        const val PASSWORD = "password"

        /** SharedPreferences『MACHINE TITLE』KEY */
        const val MACHINE_TITLE = "machine_title"

        /** 以Volatile註解表示此INSTANCE變數僅會在主記憶體中讀寫，可避免進入cache被不同執行緒讀寫而造成問題 */
        @Volatile
        private var INSTANCE: SharedPreferencesRepo? = null

        @Synchronized
        fun getInstance(context: Context): SharedPreferencesRepo {
            if (INSTANCE == null) {
                INSTANCE = SharedPreferencesRepo(context)
            }
            return INSTANCE!!
        }
    }

    /** mScheduler Server domain */
    var mSchedulerServerDomain: String
        get() =
            sharedPreferences.getString(MS_SERVER_DOMAIN, null) ?: BuildConfig.MS_DOMAIN
        set(value) {
            sharedPreferences.edit()
                .putString(MS_SERVER_DOMAIN, value)
                .apply()

            localBroadcastManager.sendBroadcast(
                Intent(MS_SERVER_DOMAIN).apply {
                    putExtra(MS_SERVER_DOMAIN, value)
                }
            )
        }

    /** clinic id */
    var orgId: String
        get() =
            sharedPreferences.getString(ORG_ID, null) ?: BuildConfig.ORG_ID
        set(value) {
            sharedPreferences.edit()
                .putString(ORG_ID, value)
                .apply()

            localBroadcastManager.sendBroadcast(
                Intent(ORG_ID).apply {
                    putExtra(ORG_ID, value)
                }
            )
        }

    /** specify clinic doctor id */
    var doctors: Set<String>
        get() =
            sharedPreferences.getStringSet(DOCTORS, emptySet()) ?: emptySet()
        set(value) {
            sharedPreferences.edit()
                .putStringSet(DOCTORS, value)
                .apply()

            localBroadcastManager.sendBroadcast(
                Intent(DOCTORS).apply {
                    putExtra(DOCTORS, value.toTypedArray())
                }
            )
        }

    /** specify clinic room id */
    var rooms: Set<String>
        get() =
            sharedPreferences.getStringSet(ROOMS, emptySet()) ?: emptySet()
        set(value) {
            sharedPreferences.edit()
                .putStringSet(ROOMS, value)
                .apply()

            localBroadcastManager.sendBroadcast(
                Intent(ROOMS).apply {
                    putExtra(ROOMS, value.toTypedArray())
                }
            )
        }


    /** queue board url */
    val clinicPanelUrl: String
        get() =
            sharedPreferences.getString(CLINIC_PANEL_MODE, null) ?: ""

    /** clinic LOGO */
    var logoUrl: String?
        get() =
            sharedPreferences.getString(LOGO_URL, null)
        set(value) {
            sharedPreferences.edit()
                .putString(LOGO_URL, value)
                .apply()

            localBroadcastManager.sendBroadcast(
                Intent(LOGO_URL).apply {
                    putExtra(LOGO_URL, value)
                }
            )
        }

    /** manual check-in serial number */
    var checkInSerialNo: Int
        get() =
            sharedPreferences.getInt(CHECK_IN_SERIAL_NO, 0)
        set(value) {
            sharedPreferences.edit()
                .putInt(CHECK_IN_SERIAL_NO, value)
                .apply()

            localBroadcastManager.sendBroadcast(
                Intent(CHECK_IN_SERIAL_NO).apply {
                    putExtra(CHECK_IN_SERIAL_NO, value)
                }
            )
        }

    /** patient national id pattern */
    var formatCheckedList: List<CreateAppointmentRequest.NationalIdFormat>
        get() =
            sharedPreferences.getStringSet(FORMAT_CHECKED_LIST, emptySet())
                ?.map { CreateAppointmentRequest.NationalIdFormat.valueOf(it) }
                ?.toList()
                ?.sortedBy { it.ordinal }
                .orEmpty()
        set(value) {
            sharedPreferences.edit()
                .putStringSet(FORMAT_CHECKED_LIST, value.map { it.name }.toSet())
                .apply()

            localBroadcastManager.sendBroadcast(
                Intent(ROOMS).apply {
                    putExtra(ROOMS, value.toTypedArray())
                }
            )
        }

    /** manual check-in item on [HomeFragment] */
    var checkInItemList: List<EditCheckInItemDialog.EditCheckInItem>
        get() = sharedPreferences.getString(CHECK_IN_ITEM_LIST, null)
            ?.takeIf { it.isNotBlank() }
            ?.let {
                Json.decodeFromString(
                    ListSerializer(EditCheckInItemDialog.EditCheckInItem.serializer()),
                    it
                )
            } ?: EditCheckInItemDialog.getEmptyCheckInItem
        set(value) {
            val json = Json.encodeToString(value)
            sharedPreferences.edit()
                .putString(CHECK_IN_ITEM_LIST, json)
                .apply()

            localBroadcastManager.sendBroadcast(
                Intent(CHECK_IN_ITEM_LIST).apply {
                    putExtra(CHECK_IN_ITEM_LIST, json)
                }
            )
        }

    /** DEPARTMENT ID*/
    var deptId: Set<String>
        get() =
            sharedPreferences.getStringSet(DEPT_ID, emptySet()) ?: emptySet()
        set(value) {
            sharedPreferences.edit()
                .putStringSet(DEPT_ID, value)
                .apply()

            localBroadcastManager.sendBroadcast(
                Intent(DEPT_ID).apply {
                    putExtra(DEPT_ID, value.toTypedArray())
                }
            )
        }


    /** QUEUEING BOARD SETTING URL */
    var queueingBoardSetting: QueuingBoardSettingModel
        get() {
            val isEnabled = sharedPreferences.getBoolean(CLINIC_PANEL_MODE_IS_ENABLED, false)
            val url = sharedPreferences.getString(CLINIC_PANEL_MODE, null) ?: ""
            return QueuingBoardSettingModel(isEnabled = isEnabled, url = url)
        }
        set(value) {
            sharedPreferences.edit()
                .putBoolean(CLINIC_PANEL_MODE_IS_ENABLED, value.isEnabled)
                .apply()

            sharedPreferences.edit()
                .putString(CLINIC_PANEL_MODE, value.url)
                .apply()

            localBroadcastManager.sendBroadcast(
                Intent(QUEUEING_BOARD_SETTING).apply {
                    putExtra(CLINIC_PANEL_MODE, value.url)
                    putExtra(CLINIC_PANEL_MODE_IS_ENABLED, value.isEnabled)
                }
            )

            localBroadcastManager.sendBroadcast(
                Intent(CLINIC_PANEL_MODE).apply {
                    putExtra(CLINIC_PANEL_MODE, value.url)
                }
            )
        }


    val queueingBoardSettingIsEnable: Boolean
        get() = sharedPreferences.getBoolean(CLINIC_PANEL_MODE_IS_ENABLED, false)

    /** QUEUEING MACHINE SETTING */
    var queueingMachineSetting: QueueingMachineSettingModel
        get() {
            val isEnable: Boolean =
                sharedPreferences.getBoolean(QUEUEING_MACHINE_SETTING_IS_ENABLE, false)
            val organization: Boolean =
                sharedPreferences.getBoolean(QUEUEING_MACHINE_SETTING_ORGANIZATION, false)
            val doctor: Boolean =
                sharedPreferences.getBoolean(QUEUEING_MACHINE_SETTING_DOCTOR, false)
            val dept: Boolean =
                sharedPreferences.getBoolean(QUEUEING_MACHINE_SETTING_DEPT, false)
            val time: Boolean =
                sharedPreferences.getBoolean(QUEUEING_MACHINE_SETTING_TIME, false)
            val isOneTicket: Boolean =
                sharedPreferences.getBoolean(QUEUEING_MACHINE_SETTING_IS_ONE_TICKET, false)

            return QueueingMachineSettingModel(
                isEnabled = isEnable,
                organization = organization,
                doctor = doctor,
                dept = dept,
                time = time,
                isOneTicket = isOneTicket
            )
        }
        set(value) {
            sharedPreferences.edit()
                .putBoolean(QUEUEING_MACHINE_SETTING_IS_ENABLE, value.isEnabled)
                .putBoolean(QUEUEING_MACHINE_SETTING_ORGANIZATION, value.organization)
                .putBoolean(QUEUEING_MACHINE_SETTING_DOCTOR, value.doctor)
                .putBoolean(QUEUEING_MACHINE_SETTING_DEPT, value.dept)
                .putBoolean(QUEUEING_MACHINE_SETTING_TIME, value.time)
                .putBoolean(QUEUEING_MACHINE_SETTING_IS_ONE_TICKET, value.isOneTicket)
                .apply()

            localBroadcastManager.sendBroadcast(
                Intent(QUEUEING_MACHINE_SETTING_ORGANIZATION).apply {
                    putExtra(QUEUEING_MACHINE_SETTING_IS_ENABLE, value.isEnabled)
                    putExtra(QUEUEING_MACHINE_SETTING_ORGANIZATION, value.organization)
                    putExtra(QUEUEING_MACHINE_SETTING_DOCTOR, value.doctor)
                    putExtra(QUEUEING_MACHINE_SETTING_DEPT, value.dept)
                    putExtra(QUEUEING_MACHINE_SETTING_TIME, value.time)
                    putExtra(QUEUEING_MACHINE_SETTING_IS_ONE_TICKET, value.isOneTicket)
                }
            )
        }

    val queueingMachineSettingIsEnable: Boolean
        get() = sharedPreferences.getBoolean(QUEUEING_MACHINE_SETTING_IS_ENABLE, false)


    var automaticAppointmentSetting: AutomaticAppointmentSettingModel
        get() {

            val isEnable: Boolean =
                sharedPreferences.getBoolean(AUTOMATIC_APPOINTMENT_SETTING_IS_ENABLE, false)
            val doctor: String =
                sharedPreferences.getString(AUTOMATIC_APPOINTMENT_SETTING_DOCTOR, "") ?: ""
            val room: String =
                sharedPreferences.getString(AUTOMATIC_APPOINTMENT_SETTING_ROOM, "") ?: ""
            val autoCheckIn: Boolean =
                sharedPreferences.getBoolean(AUTOMATIC_APPOINTMENT_SETTING_AUTO_CHECK_IN, true)

            return AutomaticAppointmentSettingModel(
                isEnabled = isEnable,
                doctorId = doctor,
                roomId = room,
                autoCheckIn = autoCheckIn
            )
        }
        set(value) {
            sharedPreferences.edit()
                .putBoolean(AUTOMATIC_APPOINTMENT_SETTING_IS_ENABLE, value.isEnabled)
                .putString(AUTOMATIC_APPOINTMENT_SETTING_DOCTOR, value.doctorId)
                .putString(AUTOMATIC_APPOINTMENT_SETTING_ROOM, value.roomId)
                .putBoolean(AUTOMATIC_APPOINTMENT_SETTING_AUTO_CHECK_IN, value.autoCheckIn)
                .apply()

            localBroadcastManager.sendBroadcast(
                Intent(AUTOMATIC_APPOINTMENT_SETTING_IS_ENABLE).apply {
                    putExtra(AUTOMATIC_APPOINTMENT_SETTING_IS_ENABLE, value.isEnabled)
                    putExtra(AUTOMATIC_APPOINTMENT_SETTING_DOCTOR, value.doctorId)
                    putExtra(AUTOMATIC_APPOINTMENT_SETTING_ROOM, value.roomId)
                    putExtra(AUTOMATIC_APPOINTMENT_SETTING_AUTO_CHECK_IN, value.autoCheckIn)
                }
            )
        }

    /** LANGUAGE */
    var language: String
        get() = sharedPreferences.getString(LANGUAGE_KEY, null) ?: BuildConfig.DEFAULT_LANGUAGE
        set(value) {
            sharedPreferences.edit()
                .putString(LANGUAGE_KEY, value)
                .apply()

            localBroadcastManager.sendBroadcast(
                Intent(LANGUAGE_KEY).apply {
                    putExtra(LANGUAGE_KEY, value)
                }
            )
        }


    /** PASSWORD */
    var password: String
        get() = sharedPreferences.getString(PASSWORD, null) ?: BuildConfig.DEFAULT_PASSWORD
        set(value) {
            sharedPreferences.edit()
                .putString(PASSWORD, value)
                .apply()

            localBroadcastManager.sendBroadcast(
                Intent(PASSWORD).apply {
                    putExtra(PASSWORD, value)
                }
            )
        }

    /** MACHINE TITLE */
    var machineTitle: String
        get() = sharedPreferences.getString(MACHINE_TITLE, null) ?: ""
        set(value) {
            sharedPreferences.edit()
                .putString(MACHINE_TITLE, value)
                .apply()

            localBroadcastManager.sendBroadcast(
                Intent(MACHINE_TITLE).apply {
                    putExtra(MACHINE_TITLE, value)
                }
            )
        }
}