package com.acatapps.videomaker.ui.splash

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.acatapps.videomaker.R
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.base.BaseActivity
import com.acatapps.videomaker.ui.HomeActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash)

       Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, HomeActivity::class.java).apply {
            }
            startActivity(intent)
            finish()
        }, 1500)



    }


 
}
