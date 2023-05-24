package com.advmeds.cliniccheckinapp.repositories

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.advmeds.cliniccheckinapp.BuildConfig
import com.advmeds.cliniccheckinapp.dialog.EditCheckInItemDialog
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueueingMachineSettingModel
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
        /** 從SharedPreferences取得『伺服器網域』的KEY */
        const val MS_SERVER_DOMAIN = "ms_server_domain"

        /** 從SharedPreferences取得『機構代碼』的KEY */
        const val ORG_ID = "org_id"

        /** 從SharedPreferences取得『綁定的醫師』的KEY */
        const val DOCTORS = "doctors"

        /** 從SharedPreferences取得『綁定的診間』的KEY */
        const val ROOMS = "rooms"

        /** 從SharedPreferences取得『叫號面板網址』的KEY */
        const val CLINIC_PANEL_MODE = "clinic_panel_mode"

        /** 從SharedPreferences取得『機構LOGO連結』的KEY */
        const val LOGO_URL = "logo_url"

        /** 從SharedPreferences取得『用來直接取號的流水號』的KEY */
        const val CHECK_IN_SERIAL_NO = "check_in_serial_no"

        /** 從SharedPreferences取得『取得病患資訊的national_id可輸入格式』的KEY */
        const val FORMAT_CHECKED_LIST = "format_checked_list"

        /** 從SharedPreferences取得『首頁的直接取號項目』的KEY */
        val CHECK_IN_ITEM_LIST = "check_in_item_list"

        /** 從SharedPreferences取得『Department ID』的KEY */
        const val DEPT_ID = "dept_id"

        /** SharedPreferences [queue machine setting params] KEY */
        const val QUEUEING_MACHINE_SETTING_ORGANIZATION = "queueing_machine_setting_organization"
        const val QUEUEING_MACHINE_SETTING_DOCTOR = "queueing_machine_setting_organization_doctor"
        const val QUEUEING_MACHINE_SETTING_DEPT = "queueing_machine_setting_organization_dept"
        const val QUEUEING_MACHINE_SETTING_TIME = "queueing_machine_setting_organization_time"

        /** SharedPreferences『Language』KEY */
        const val LANGUAGE_KEY = "language"

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

    /** 雲排伺服器網域 */
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

    /** 機構代碼 */
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

    /** 綁定的醫師 */
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

    /** 綁定的診間 */
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

    /** 叫號面板網址 */
    var clinicPanelUrl: String?
        get() =
            sharedPreferences.getString(CLINIC_PANEL_MODE, null)
        set(value) {
            sharedPreferences.edit()
                .putString(CLINIC_PANEL_MODE, value)
                .apply()

            localBroadcastManager.sendBroadcast(
                Intent(CLINIC_PANEL_MODE).apply {
                    putExtra(CLINIC_PANEL_MODE, value)
                }
            )
        }

    /** 機構LOGO */
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

    /** 小兒流水號 */
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

    /** 取得病患資訊的national_id可輸入格式 */
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

    var checkInItemList: List<EditCheckInItemDialog.EditCheckInItem>
        get() = sharedPreferences.getString(CHECK_IN_ITEM_LIST, null)
            ?.takeIf { it.isNotBlank() }
            ?.let {
                Json.decodeFromString(
                    ListSerializer(EditCheckInItemDialog.EditCheckInItem.serializer()),
                    it
                )
            }.orEmpty()
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

    /** QUEUEING MACHINE SETTING */
    var queueingMachineSetting: QueueingMachineSettingModel
        get() {
            val organization: Boolean =
                sharedPreferences.getBoolean(QUEUEING_MACHINE_SETTING_ORGANIZATION, false)
            val doctor: Boolean =
                sharedPreferences.getBoolean(QUEUEING_MACHINE_SETTING_DOCTOR, false)
            val dept: Boolean = sharedPreferences.getBoolean(QUEUEING_MACHINE_SETTING_DEPT, false)
            val time: Boolean = sharedPreferences.getBoolean(QUEUEING_MACHINE_SETTING_TIME, false)

            return QueueingMachineSettingModel(
                organization = organization,
                doctor = doctor,
                dept = dept,
                time = time
            )
        }
        set(value) {
            sharedPreferences.edit()
                .putBoolean(QUEUEING_MACHINE_SETTING_ORGANIZATION, value.organization)
                .apply()

            sharedPreferences.edit()
                .putBoolean(QUEUEING_MACHINE_SETTING_DOCTOR, value.doctor)
                .apply()

            sharedPreferences.edit()
                .putBoolean(QUEUEING_MACHINE_SETTING_DEPT, value.dept)
                .apply()

            sharedPreferences.edit()
                .putBoolean(QUEUEING_MACHINE_SETTING_TIME, value.time)
                .apply()

            localBroadcastManager.sendBroadcast(
                Intent(DEPT_ID).apply {
                    putExtra(QUEUEING_MACHINE_SETTING_ORGANIZATION, value.organization)
                    putExtra(QUEUEING_MACHINE_SETTING_DOCTOR, value.doctor)
                    putExtra(QUEUEING_MACHINE_SETTING_DEPT, value.dept)
                    putExtra(QUEUEING_MACHINE_SETTING_TIME, value.time)
                }
            )
        }

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

}