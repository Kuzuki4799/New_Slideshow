package com.hope_studio.video_maker_slideshow_maker.ui.splash

import com.photo.slideshow.BaseSActivity
import com.hope_studio.video_maker_slideshow_maker.BuildConfig
import com.hope_studio.video_maker_slideshow_maker.R
import com.studio.maker.HomeActivity

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
