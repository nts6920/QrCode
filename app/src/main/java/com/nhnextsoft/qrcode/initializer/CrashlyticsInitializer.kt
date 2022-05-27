package com.nhnextsoft.qrcode.initializer

import android.content.Context
import androidx.startup.Initializer

class CrashlyticsInitializer : Initializer<Unit> {

    override fun create(context: Context) {
//        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}