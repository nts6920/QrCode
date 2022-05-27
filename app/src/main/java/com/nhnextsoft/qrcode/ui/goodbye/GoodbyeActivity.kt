package com.nhnextsoft.qrcode.ui.goodbye

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import androidx.databinding.DataBindingUtil
import com.nhnextsoft.qrcode.R
import com.nhnextsoft.qrcode.databinding.ActivityGoodbyeBinding

class GoodbyeActivity : AppCompatActivity() {

    var timer: CountDownTimer = object : CountDownTimer(3000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            (getString(R.string.exit) + "..." + (millisUntilFinished / 1000) + "s").also {
                binding.tvExit.text = it
            }
        }

        override fun onFinish() {
            if (isFinishing.not()) {
                finishAffinity()
            }
        }
    }


    private lateinit var binding: ActivityGoodbyeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_goodbye)
        setTheme(R.style.Theme_QRCode_NoActionBar)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_goodbye)
    }



    override fun onResume() {
        super.onResume()
        timer.start()
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
    }


    override fun onBackPressed() {
        timer.cancel()
        finishAffinity()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, GoodbyeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            return intent
        }
    }
}