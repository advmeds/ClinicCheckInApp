package com.advmeds.cliniccheckinapp.ui

import android.app.Application
import com.advmeds.cliniccheckinapp.BuildConfig
import com.advmeds.cliniccheckinapp.dialog.EditCheckInItemDialog
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo
import timber.log.Timber

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initTimber()
        initCheckInItem()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String =
                    "(${element.fileName}:${element.lineNumber})#${element.methodName}"
            })
        }
    }

    /** 根據場域初始化取號項目 */
    private fun initCheckInItem() {
        val sharedPreferencesRepo = SharedPreferencesRepo.getInstance(this)

        if (sharedPreferencesRepo.checkInItemList.all { !it.isShow }) {
            when(BuildConfig.BUILD_TYPE) {
                "ptch" -> {
                    sharedPreferencesRepo.checkInItemList = listOf(
                        EditCheckInItemDialog.EditCheckInItem(
                            type = EditCheckInItemDialog.CheckInItemType.MANUAL_INPUT,
                            isShow = true
                        ),
                        EditCheckInItemDialog.EditCheckInItem(
                            type = EditCheckInItemDialog.CheckInItemType.CUSTOM_ONE,
                            isShow = true,
                            title = "小兒心臟超音波",
                            doctorId = "CA",
                            divisionId = "0000"
                        )
                    )
                }
                "rende" -> {
                    sharedPreferencesRepo.checkInItemList = listOf(
                        EditCheckInItemDialog.EditCheckInItem(
                            type = EditCheckInItemDialog.CheckInItemType.CUSTOM_ONE,
                            isShow = true,
                            title = "疫苗施打",
                            doctorId = "e666",
                            divisionId = "398"
                        ),
                        EditCheckInItemDialog.EditCheckInItem(
                            type = EditCheckInItemDialog.CheckInItemType.CUSTOM_TWO,
                            isShow = true,
                            title = "體檢",
                            doctorId = "e666",
                            divisionId = "400"
                        )
                    )
                }
                else -> {
                    sharedPreferencesRepo.checkInItemList = listOf(
                        EditCheckInItemDialog.EditCheckInItem(
                            type = EditCheckInItemDialog.CheckInItemType.MANUAL_INPUT,
                            isShow = true
                        ),
                        EditCheckInItemDialog.EditCheckInItem(
                            type = EditCheckInItemDialog.CheckInItemType.VIRTUAL_CARD,
                            isShow = true
                        )
                    )
                }
            }
        }
    }
}