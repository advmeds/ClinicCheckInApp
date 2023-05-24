package com.advmeds.cliniccheckinapp.repositories

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.advmeds.cliniccheckinapp.BuildConfig
import com.advmeds.cliniccheckinapp.dialog.EditCheckInItemDialog
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
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
        const val CLINIC_PANEL_MODE = "clinic_panel_mode"

        /** clinic LOGO url key of SharedPreferences */
        const val LOGO_URL = "logo_url"

        /** manual check-in serial number key of SharedPreferences */
        const val CHECK_IN_SERIAL_NO = "check_in_serial_no"

        /** patient national id pattern key of SharedPreferences */
        const val FORMAT_CHECKED_LIST = "format_checked_list"

        /** manual check-in item key of SharedPreferences */
        const val CHECK_IN_ITEM_LIST = "check_in_item_list"

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
                Json.decodeFromString(ListSerializer(EditCheckInItemDialog.EditCheckInItem.serializer()), it)
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
}