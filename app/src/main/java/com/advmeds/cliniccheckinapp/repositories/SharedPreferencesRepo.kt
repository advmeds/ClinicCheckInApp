package com.advmeds.cliniccheckinapp.repositories

import android.content.Context
import android.content.SharedPreferences
import com.advmeds.cliniccheckinapp.BuildConfig

class SharedPreferencesRepo(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        /** 從SharedPreferences取得『伺服器網域』的KEY */
        const val MS_SERVER_DOMAIN = "ms_server_domain"

        /** 從SharedPreferences取得『機構代碼』的KEY */
        const val ORG_ID = "org_id"

        /** 從SharedPreferences取得『綁定的診間』的KEY */
        const val ROOMS = "rooms"

        /** 從SharedPreferences取得『叫號面板網址』的KEY */
        const val CLINIC_PANEL_MODE = "clinic_panel_mode"

        /** 從SharedPreferences取得『機構LOGO連結』的KEY */
        const val LOGO_URL = "logo_url"

        /** 從SharedPreferences取得『用來直接取號的流水號』的KEY */
        const val CHECK_IN_SERIAL_NO = "check_in_serial_no"

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

    /** 機構代碼 */
    var orgId: String
        get() =
            sharedPreferences.getString(ORG_ID, null) ?: BuildConfig.ORG_ID
        set(value) =
            sharedPreferences.edit()
                .putString(ORG_ID, value)
                .apply()

    /** 綁定的診間 */
    var rooms: Set<String>
        get() =
            sharedPreferences.getStringSet(ROOMS, emptySet()) ?: emptySet()
        set(value) =
            sharedPreferences.edit()
                .putStringSet(ROOMS, value)
                .apply()

    /** 叫號面板網址 */
    var clinicPanelUrl: String?
        get() =
            sharedPreferences.getString(CLINIC_PANEL_MODE, null)
        set(value) =
            sharedPreferences.edit()
                .putString(CLINIC_PANEL_MODE, value)
                .apply()

    /** 機構LOGO */
    var logoUrl: String?
        get() =
            sharedPreferences.getString(LOGO_URL, null)
        set(value) =
            sharedPreferences.edit()
                .putString(LOGO_URL, value)
                .apply()

    /** 小兒流水號 */
    var checkInSerialNo: Int
        get() =
            sharedPreferences.getInt(CHECK_IN_SERIAL_NO, 0)
        set(value) =
            sharedPreferences.edit()
                .putInt(CHECK_IN_SERIAL_NO, value)
                .apply()
}