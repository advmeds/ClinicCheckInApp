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
}