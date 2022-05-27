package com.nhnextsoft.qrcode.utils

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat.getSystemService
import timber.log.Timber

object VibratorHelper {

    fun vibrateDevice(context: Context) {
        val vibratePattern = 500L

        Timber.d("QRCodeAnalyzer vibrateDevice")
        val vibrator = getSystemService(context, Vibrator::class.java)
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(vibratePattern,
                    VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(vibratePattern)
            }
        }
    }

    // Vibrate for 150 milliseconds
    fun shakeItBaby(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(context, Vibrator::class.java) as Vibrator).vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            (getSystemService(context, Vibrator::class.java) as Vibrator).vibrate(150)
        }
    }
}