package com.advmeds.cliniccheckinapp.repositories

import android.content.Context
import android.content.SharedPreferences
import com.advmeds.cliniccheckinapp.BuildConfig
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharePreferences.QueueingMachineSettingModel

class SharedPreferencesRepo(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        /** 從SharedPreferences取得『伺服器網域』的KEY */
        const val MS_SERVER_DOMAIN = "ms_server_domain"

        /** 從SharedPreferences取得『機構代碼』的KEY */
        const val ORG_ID = "org_id"

        const val DOCTOR_IDS = "doctor_id"
        const val ROOM_IDS = "room_id"
        const val DEPT_ID = "dept_id"

        const val QUEUEING_MACHINE_SETTING_ORGANIZATION = "queueing_machine_setting_organization"
        const val QUEUEING_MACHINE_SETTING_DOCTOR= "queueing_machine_setting_organization_doctor"
        const val QUEUEING_MACHINE_SETTING_DEPT = "queueing_machine_setting_organization_dept"
        const val QUEUEING_MACHINE_SETTING_TIME = "queueing_machine_setting_organization_time"

        const val LANGUAGE_KEY = "language"

        /** 以Volatile註解表示此INSTANCE變數僅會在主記憶體中讀寫，可避免進入cache被不同執行緒讀寫而造成問題 */
        @Volatile
        private var INSTANCE: SharedPreferencesRepo? = null

        @Synchronized
        fun getInstance(context: Context): SharedPreferencesRepo {
            if (INSTANCE == null) {
                INSTANCE = SharedPreferencesRepo(
                    context.getSharedPreferences(
                        BuildConfig.APPLICATION_ID,
                        Context.MODE_PRIVATE
                    )
                )
            }
            return INSTANCE!!
        }
    }

    /** 雲排伺服器網域 */
    var mSchedulerServerDomain: String
        get() =
            sharedPreferences.getString(MS_SERVER_DOMAIN, null) ?: BuildConfig.MS_DOMAIN
        set(value) =
            sharedPreferences.edit()
                .putString(MS_SERVER_DOMAIN, value)
                .apply()

    var orgId: String
        get() =
            sharedPreferences.getString(ORG_ID, null) ?: BuildConfig.ORG_ID
        set(value) =
            sharedPreferences.edit()
                .putString(ORG_ID, value)
                .apply()

    var doctorIds: List<String>
        get() {
            val setString = sharedPreferences.getStringSet(DOCTOR_IDS, null) ?: emptySet()
            return setString.toList()
        }
        set(value) {
            val stringSet = value.toSet()
            sharedPreferences.edit()
                .putStringSet(DOCTOR_IDS, stringSet)
                .apply()
        }

    var roomIds: List<String>
        get() {
            val setString = sharedPreferences.getStringSet(ROOM_IDS, null) ?: emptySet()
            return setString.toList()
        }
        set(value) {
            val stringSet = value.toSet()
            sharedPreferences.edit()
                .putStringSet(ROOM_IDS, stringSet)
                .apply()
        }

    var deptId: String
        get() = sharedPreferences.getString(DEPT_ID, null) ?: ""
        set(value) =
            sharedPreferences.edit()
                .putString(ROOM_IDS, value)
                .apply()

    var queueingMachineSetting: QueueingMachineSettingModel
        get() {
            val organization: Boolean = sharedPreferences.getBoolean(QUEUEING_MACHINE_SETTING_ORGANIZATION, false)
            val doctor: Boolean = sharedPreferences.getBoolean(QUEUEING_MACHINE_SETTING_DOCTOR, false)
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
        }

    var language: String
        get() = sharedPreferences.getString(LANGUAGE_KEY, null) ?: BuildConfig.DEFAULT_LANGUAGE
        set(value) =
            sharedPreferences.edit()
                .putString(LANGUAGE_KEY, value)
                .apply()
}