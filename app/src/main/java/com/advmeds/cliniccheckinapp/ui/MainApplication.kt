package com.advmeds.cliniccheckinapp.ui

import android.app.Application
import com.advmeds.cliniccheckinapp.BuildConfig
import timber.log.Timber

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initTimber()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String =
                    "(${element.fileName}:${element.lineNumber})#${element.methodName}"
            })
        }
    }
}