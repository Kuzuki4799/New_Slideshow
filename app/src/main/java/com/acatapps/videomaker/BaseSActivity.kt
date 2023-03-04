package com.acatapps.videomaker

import android.app.Activity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import com.hope_studio.base_ads.BuildConfig
import com.hope_studio.base_ads.ads.BaseAds
import com.hope_studio.base_ads.base.BaseActivity
import com.hope_studio.base_ads.base.BaseApplication
import com.hope_studio.base_ads.model.DataModel
import com.hope_studio.base_ads.utils.BLog
import com.hope_studio.base_ads.utils.BillingUtils

abstract class BaseSActivity : BaseActivity() {

    abstract fun codeApp(): Int

    abstract fun url(): String

    abstract fun gotoMain(): Class<*>

    private var countDownTimer: CountDownTimer? = null

    private var isGone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullScreencall()
        BLog.d("activity: " + this.javaClass.simpleName)

        getApi(this)
        handlerTimeDown()
    }

    private fun handlerTimeDown() {
        countDownTimer = object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("base_main_ads", "Splash $millisUntilFinished")
            }

            override fun onFinish() {
                isGone = true
                openNewActivity(gotoMain(), isShowAds = true, isFinish = true)
                if (BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Splash Finish")
                }
            }
        }.start()
    }

    private fun handlerIntent(isShow: Boolean) {
        countDownTimer?.cancel()
        openNewActivity(gotoMain(), isShow, isFinish = true)
        if (BuildConfig.DEBUG) {
            Log.d("base_main_ads", "Go Main")
        }
        if (!isShow) {
            if (BaseAds.getPreLoad(this)) {
                BaseAds.loadInterstitialAd(this, 0)
            }
        }
    }

    private fun getApi(activity: Activity) {
        getBaseApplication()?.setEnableOpen(false)
        BaseAds.resetData(this)
        BaseAds.callApiAds(this, codeApp(), url(), object : BaseAds.OnCallApiCallback {
            override fun onCallSuccess(result: DataModel) {
                if (result.getSizeAds() == 0 || !result.getShowAds()) {
                    handlerIntent(true)
                } else if (BillingUtils.getDataBilling(this@BaseSActivity)) {
                    handlerIntent(false)
                } else {
                    BaseAds.initAds(this@BaseSActivity, result)
                    getBaseApplication()?.setEnableOpen(false)

                    if (result.getShowFirstOpen()) {
                        if (result.getSplashOpen() && result.getShowOpenAds()) {
                            getBaseApplication()?.loadAndShow(this@BaseSActivity, object :
                                BaseApplication.OnShowAdListener {
                                override fun onLoadSuccess() {
                                    if (!isGone) {
                                        getBaseApplication()?.showAdIfAvailable(
                                            this@BaseSActivity, this
                                        )
                                    }
                                }

                                override fun onLoadFail() {
                                    if (!isGone) handlerIntent(result.getShowInterstitialAds())
                                }

                                override fun onShowAdComplete() {
                                    countDownTimer?.cancel()
                                }

                                override fun onShowAdDismiss() {
                                    if (!isGone) handlerIntent(false)
                                }

                                override fun onShowAdError() {
                                    if (!isGone) handlerIntent(result.getShowInterstitialAds())
                                }
                            })
                        } else {
                            if (!isGone) handlerIntent(result.getShowInterstitialAds())
                        }
                    } else {
                        if (!isGone) handlerIntent(false)
                    }
                }
            }

            override fun onCallFail() {
                if (!isGone) handlerIntent(true)
            }
        })
    }
}