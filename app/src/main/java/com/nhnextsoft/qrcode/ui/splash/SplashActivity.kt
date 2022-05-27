package com.nhnextsoft.qrcode.ui.splash

import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import com.nhnextsoft.qrcode.R
import com.nhnextsoft.qrcode.ui.feature.entry.QrEntryPointCodeActivity

class SplashActivity : AppCompatActivity() {

    private val timerGotoHome = object : CountDownTimer(5000, 500) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            gotoHome()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        gotoHome()
    }


    @OptIn(ExperimentalAnimationApi::class)
    private fun gotoHome() {
        // have pattern
        // goto home page
        startActivity(QrEntryPointCodeActivity.newIntent(this))
        finish()
    }

}