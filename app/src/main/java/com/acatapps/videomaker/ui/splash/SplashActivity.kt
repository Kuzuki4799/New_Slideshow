package com.acatapps.videomaker.ui.splash

import com.acatapps.videomaker.BaseSActivity
import com.acatapps.videomaker.BuildConfig
import com.acatapps.videomaker.R
import com.acatapps.videomaker.ui.HomeActivity

class SplashActivity : BaseSActivity() {

    override fun codeApp(): Int {
        return BuildConfig.CODE_APP
    }

    override fun url(): String {
        return BuildConfig.API_URL
    }

    override fun gotoMain(): Class<*> {
        return HomeActivity::class.java
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_splash
    }
}
