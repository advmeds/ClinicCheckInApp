package com.advmeds.cliniccheckinapp.ui

import android.app.Application
import android.os.Bundle
import android.os.Looper
import com.advmeds.cliniccheckinapp.BuildConfig
import com.advmeds.cliniccheckinapp.dialog.EditCheckInItemDialog
import com.advmeds.cliniccheckinapp.repositories.AnalyticsRepository
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo
import timber.log.Timber

class MainApplication : Application() {
    override fun onCreate() {
        initUncaughtExceptionHandler()

        super.onCreate()

        countSession()
        initTimber()
        initCheckInItem()
        initCheckerOfActivityLife()
    }

    private fun countSession() {
        val sharedPreferencesRepo = SharedPreferencesRepo.getInstance(this)
        sharedPreferencesRepo.sessionNumber++
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
            when (BuildConfig.BUILD_TYPE) {
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


    private fun initUncaughtExceptionHandler() {
        val sharedPreferencesRepo = SharedPreferencesRepo.getInstance(this)
        Thread.setDefaultUncaughtExceptionHandler(MyUncaughtExceptionHandler(sharedPreferencesRepo))
    }

    private fun initCheckerOfActivityLife() {
        val sharedPreferencesRepo = SharedPreferencesRepo.getInstance(this)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(
                activity: android.app.Activity, savedInstanceState: Bundle?
            ) {}

            override fun onActivityStarted(activity: android.app.Activity) {}

            override fun onActivityResumed(activity: android.app.Activity) {}

            override fun onActivityPaused(activity: android.app.Activity) {}

            override fun onActivityStopped(activity: android.app.Activity) {}

            override fun onActivitySaveInstanceState(
                activity: android.app.Activity,
                outState: Bundle
            ) {}

            override fun onActivityDestroyed(activity: android.app.Activity) {
                val map = mutableMapOf<String, Any>()
                map[AnalyticsRepository.SOURCE_SCREEN] = "MainApplication"
                map[AnalyticsRepository.SOURCE_ACTION] = "app is closing"

                map["is it manual close"] = true

                sharedPreferencesRepo.closeAppEvent = Pair("close of the app", map)
            }
        })
    }

    private inner class MyUncaughtExceptionHandler(
        val sharedPreferencesRepo: SharedPreferencesRepo
    ) : Thread.UncaughtExceptionHandler {

        private val defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? =
            Thread.getDefaultUncaughtExceptionHandler()

        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            val reportBuilder = StringBuilder()

            reportBuilder
                .append("\n")
                .append("Current thread: $thread")
                .append("\n\n");
            processThrowable(throwable, reportBuilder)

            object : Thread() {
                override fun run() {
                    Looper.prepare()
                    saveCloseAppEvent(reportBuilder)
                    Looper.loop()
                }
            }.start()

            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) { }

            defaultUncaughtExceptionHandler?.uncaughtException(thread, throwable)
        }

        private fun saveCloseAppEvent(reportBuilder: StringBuilder) {
            val map = mutableMapOf<String, Any>()
            map[AnalyticsRepository.SOURCE_SCREEN] = "MainApplication"
            map[AnalyticsRepository.SOURCE_ACTION] = "app is closing"

            map["is it manual close"] = false
            map["error reason"] = reportBuilder.toString()

            sharedPreferencesRepo.closeAppEvent = Pair("close of the app", map)
        }

        private fun processThrowable(exception: Throwable?, builder: StringBuilder) {
            if (exception == null) return
            val stackTraceElements = exception.stackTrace
            builder
                .append("Exception: ").append(exception.javaClass.name).append("\n")
                .append("Message: ").append(exception.message).append("\nStacktrace:\n")
            for (element in stackTraceElements) {
                builder.append("\t").append(element.toString()).append("\n")
            }
            processThrowable(exception.cause, builder)
        }
    }
}