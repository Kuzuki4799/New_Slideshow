package com.hope_studio.base_ads.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.androidnetworking.AndroidNetworking
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAppOpenAd
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.facebook.ads.AdSettings
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.hope_studio.base_ads.BuildConfig
import com.hope_studio.base_ads.ads.AdsType
import com.hope_studio.base_ads.ads.BaseAds
import com.hope_studio.base_ads.model.DataModel
import com.hope_studio.base_ads.utils.AnalyticsUtils
import com.hope_studio.base_ads.utils.BillingUtils
import com.hope_studio.base_ads.utils.NetWorkUtils
import com.hope_studio.base_ads.utils.ShareUtils
import java.util.*

open class BaseApplication : Application(), Application.ActivityLifecycleCallbacks,
    LifecycleObserver {

    private val LOG_TAG = "base_main_ads"

    private var isEnable = false

    private lateinit var appOpenAdManager: AppOpenAdManager
    private var currentActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()
        AnalyticsUtils.initFirebaseAnalytic(this)
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        appOpenAdManager = AppOpenAdManager()

        AndroidNetworking.initialize(applicationContext)

        AdSettings.setTestMode(com.hope_studio.base_ads.BuildConfig.DEBUG)

        AppLovinSdk.getInstance(this).mediationProvider = AppLovinMediationProvider.MAX
        AppLovinSdk.getInstance(this).initializeSdk {
            Log.d("base_main_ads", "AppLovinSdk initializeSdk")
        }
    }

    /** LifecycleObserver method that shows the app open ad when the app moves to foreground. */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // Show the ad (if available) when the app moves to foreground.
        if (isEnable) {
            currentActivity?.let { appOpenAdManager.showAdIfAvailable(it) }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
            Log.d("base_main_ads", "onAppBackgrounded")
        }

        if (!NetWorkUtils.isNetworkConnected(this)) return
        val dataAds = ShareUtils[this, DataModel::class.java.name, DataModel::class.java]
        if (dataAds == null) return
        if (!dataAds.getShowAds()) return
        if (dataAds.getSizeAds() == 0) return
        if (currentActivity?.let { BillingUtils.getDataBilling(it) } == true) return
        if (dataAds.getShowOpenAds() && !BaseAds.isShowAds) {
            setEnableOpen(dataAds.getShowOpenAds())
            currentActivity?.let { loadAds(it) }
        }
    }

    /** ActivityLifecycleCallback methods. */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        // An ad activity is started when an ad is showing, which could be AdActivity class from Google
        // SDK or another activity class implemented by a third party mediation partner. Updating the
        // currentActivity only when an ad is not showing will ensure it is not an ad activity, but the
        // one that shows the ad.
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    /**
     * Shows an app open ad.
     *
     * @param activity the activity that shows the app open ad
     * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
     */
    fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OnShowAdListener) {
        // We wrap the showAdIfAvailable to enforce that other classes only interact with MyApplication
        // class.
        appOpenAdManager.showAdIfAvailable(activity, onShowAdCompleteListener)
    }

    fun setEnableOpen(isShow: Boolean) {
        this.isEnable = isShow
    }

    fun loadAds(activity: Activity) {
        appOpenAdManager.loadAd(activity, object : OnShowAdListener {
            override fun onLoadSuccess() {
            }

            override fun onLoadFail() {
            }

            override fun onShowAdComplete() {
            }

            override fun onShowAdDismiss() {
            }

            override fun onShowAdError() {
            }
        })
    }

    fun loadAndShow(activity: Activity, onShowAdCompleteListener: OnShowAdListener) {
        appOpenAdManager.loadAndShow(activity, 0, onShowAdCompleteListener)
    }

    /**
     * Interface definition for a callback to be invoked when an app open ad is complete
     * (i.e. dismissed or fails to show).
     */
    interface OnShowAdListener {

        fun onLoadSuccess()

        fun onLoadFail()

        fun onShowAdComplete()

        fun onShowAdDismiss()

        fun onShowAdError()
    }

    /** Inner class that loads and shows app open ads. */
    private inner class AppOpenAdManager {

        private var appOpenAd: AppOpenAd? = null
        private var applovinOpenAd: MaxAppOpenAd? = null
        private var isLoadingAd = false
        var isShowingAd = false

        /** Keep track of the time an app open ad is loaded to ensure you don't show an expired ad. */
        private var loadTime: Long = 0

        /**
         * Load an ad.
         *
         * @param context the context of the activity that loads the ad
         */
        fun loadAd(context: Context, onShowAdCompleteListener: OnShowAdListener) {
            // Do not load ad if there is an unused ad or one is already loading.
            if (isLoadingAd || isAdAvailable()) {
                onShowAdCompleteListener.onLoadFail()
                return
            }

            isLoadingAd = true
            val request = AdRequest.Builder().build()
            val dataAds =
                ShareUtils[this@BaseApplication, DataModel::class.java.name, DataModel::class.java]
            if (dataAds == null) {
                onShowAdCompleteListener.onLoadFail()
                return
            }
            if (!dataAds.getShowAds()) {
                onShowAdCompleteListener.onLoadFail()
                return
            }
            if (dataAds.getAdsByType(AdsType.ADMOB.value).type.isEmpty()) {
                onShowAdCompleteListener.onLoadFail()
                return
            }
            AppOpenAd.load(
                context, dataAds.key_open_app, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    /**
                     * Called when an app open ad has loaded.
                     *
                     * @param ad the loaded app open ad.
                     */
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time
                        if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                            Log.d("base_main_ads", "OpenApp Loaded")
                        }
                    }

                    /**
                     * Called when an app open ad has failed to load.
                     *
                     * @param loadAdError the error.
                     */
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isLoadingAd = false
                        if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                            val errMessage =
                                "OpenApp Error: ${loadAdError.message} - code + ${loadAdError.code}"
                            Log.d("base_main_ads", errMessage)
                        }

                        //Applovin
                        applovinOpenAd = MaxAppOpenAd(dataAds.key_open_app, context)
                        applovinOpenAd?.setListener(object : MaxAdListener {
                            override fun onAdLoaded(ad: MaxAd?) {
                                isLoadingAd = false
                                loadTime = Date().time
                                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                                    Log.d("base_main_ads", "OpenApp Applovin Loaded")
                                }
                            }

                            override fun onAdDisplayed(ad: MaxAd?) {
                                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                                    Log.d("base_main_ads", "OpenApp Applovin Showed")
                                }
                                onShowAdCompleteListener.onShowAdComplete()
                            }

                            override fun onAdHidden(ad: MaxAd?) {
                                applovinOpenAd = null
                                isShowingAd = false
                                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                                    Log.d("base_main_ads", "OpenApp Applovin Dismiss")
                                }
                                onShowAdCompleteListener.onShowAdDismiss()
                            }

                            override fun onAdClicked(ad: MaxAd?) {
                            }

                            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                                applovinOpenAd = null
                                isLoadingAd = false
                                isShowingAd = false
                                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                                    val errMessage =
                                        "OpenApp Applovin Error: ${error?.message} - code + ${error?.code}"
                                    Log.d("base_main_ads", errMessage)
                                }
                                onShowAdCompleteListener.onLoadFail()
                            }

                            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                                applovinOpenAd = null
                                isShowingAd = false

                                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                                    val errMessage =
                                        "Open Applovin Show Error: ${error?.message} - code + ${error?.code}"
                                    Log.d("base_main_ads", errMessage)
                                }
                                onShowAdCompleteListener.onShowAdError()
                            }
                        })
                        applovinOpenAd?.loadAd()
                    }
                })
        }

        fun loadAndShow(
            context: Activity, position: Int, onShowAdCompleteListener: OnShowAdListener
        ) {
            if (isLoadingAd || isAdAvailable()) {
                return
            }

            isLoadingAd = true
            val request = AdRequest.Builder().build()
            val dataAds =
                ShareUtils[this@BaseApplication, DataModel::class.java.name, DataModel::class.java]
            if (dataAds == null) {
                onShowAdCompleteListener.onLoadFail()
                return
            }
            if (!dataAds.getShowAds()) {
                onShowAdCompleteListener.onLoadFail()
                return
            }
            if (dataAds.getAdsByType(AdsType.ADMOB.value).type.isEmpty()) {
                onShowAdCompleteListener.onLoadFail()
                return
            }
            if (position == dataAds.getSizeAds()) {
                onShowAdCompleteListener.onLoadFail()
                return
            }

            AppOpenAd.load(
                context, dataAds.key_open_app, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    /**
                     * Called when an app open ad has loaded.
                     *
                     * @param ad the loaded app open ad.
                     */
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time
                        if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                            Log.d("base_main_ads", "OpenApp Loaded")
                        }
                        onShowAdCompleteListener.onLoadSuccess()
                    }

                    /**
                     * Called when an app open ad has failed to load.
                     *
                     * @param loadAdError the error.
                     */
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isLoadingAd = false
                        if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                            val errMessage =
                                "OpenApp Error: ${loadAdError.message} - code + ${loadAdError.code}"
                            Log.d("base_main_ads", errMessage)
                        }

                        //APPLOVIN
                        applovinOpenAd = MaxAppOpenAd(dataAds.key_open_app, context)
                        applovinOpenAd?.setListener(object : MaxAdListener {
                            override fun onAdLoaded(ad: MaxAd?) {
                                isLoadingAd = false
                                loadTime = Date().time
                                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                                    Log.d("base_main_ads", "OpenApp Loaded")
                                }
                                onShowAdCompleteListener.onLoadSuccess()
                            }

                            override fun onAdDisplayed(ad: MaxAd?) {
                                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                                    Log.d("base_main_ads", "OpenApp Applovin Showed")
                                }
                                onShowAdCompleteListener.onShowAdComplete()
                            }

                            override fun onAdHidden(ad: MaxAd?) {
                                applovinOpenAd = null
                                isShowingAd = false
                                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                                    Log.d("base_main_ads", "OpenApp Applovin Dismiss")
                                }
                                onShowAdCompleteListener.onShowAdDismiss()
                            }

                            override fun onAdClicked(ad: MaxAd?) {
                            }

                            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                                applovinOpenAd = null
                                isLoadingAd = false
                                isShowingAd = false
                                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                                    val errMessage =
                                        "OpenApp Applovin Error: ${error?.message} - code + ${error?.code}"
                                    Log.d("base_main_ads", errMessage)
                                }
                                onShowAdCompleteListener.onLoadFail()
                            }

                            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                                applovinOpenAd = null
                                isShowingAd = false

                                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                                    val errMessage =
                                        "Open Applovin Show Error: ${error?.message} - code + ${error?.code}"
                                    Log.d("base_main_ads", errMessage)
                                }
                                onShowAdCompleteListener.onShowAdError()
                            }
                        })
                        applovinOpenAd?.loadAd()
                    }
                })
        }

        /** Check if ad was loaded more than n hours ago. */
        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
            val dateDifference: Long = Date().time - loadTime
            val numMilliSecondsPerHour: Long = 3600000
            return dateDifference < numMilliSecondsPerHour * numHours
        }

        /** Check if ad exists and can be shown. */
        private fun isAdAvailable(): Boolean {
            // Ad references in the app open beta will time out after four hours, but this time limit
            // may change in future beta versions. For details, see:
            // https://support.google.com/admob/answer/9341964?hl=en
            return applovinOpenAd != null || appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
        }

        /**
         * Show the ad if one isn't already showing.
         *
         * @param activity the activity that shows the app open ad
         */
        fun showAdIfAvailable(activity: Activity) {
            showAdIfAvailable(activity, object : OnShowAdListener {
                override fun onLoadSuccess() {
                }

                override fun onLoadFail() {
                }

                override fun onShowAdComplete() {
                }

                override fun onShowAdDismiss() {
                }

                override fun onShowAdError() {
                }
            })
        }

        /**
         * Show the ad if one isn't already showing.
         *
         * @param activity the activity that shows the app open ad
         * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
         */
        fun showAdIfAvailable(
            activity: Activity, onShowAdCompleteListener: OnShowAdListener
        ) {
            // If the app open ad is already showing, do not show the ad again.
            if (isShowingAd) {
                Log.d(LOG_TAG, "The app open ad is already showing.")
                return
            }

            if (BaseAds.isShowAds) {
                Log.d("base_main_ads", "Other Ads Show")
                return
            }

            // If the app open ad is not available yet, invoke the callback then load the ad.
            if (!isAdAvailable()) {
                Log.d(LOG_TAG, "The app open ad is not ready yet.")
                onShowAdCompleteListener.onShowAdError()
                return
            }

            Log.d(LOG_TAG, "Will show ad.")

            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                /** Called when full screen content is dismissed. */
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    appOpenAd = null
                    isShowingAd = false
                    if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                        Log.d("base_main_ads", "OpenApp Dismiss")
                    }
                    onShowAdCompleteListener.onShowAdDismiss()
                }

                /** Called when fullscreen content failed to show. */
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isShowingAd = false

                    if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                        val errMessage =
                            "Open App Show Error: ${adError.message} - code + ${adError.code}"
                        Log.d("base_main_ads", errMessage)
                    }
                    onShowAdCompleteListener.onShowAdError()
                }

                /** Called when fullscreen content is shown. */
                override fun onAdShowedFullScreenContent() {
                    if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                        Log.d("base_main_ads", "OpenApp Showed")
                    }
                    onShowAdCompleteListener.onShowAdComplete()
                }
            }

            val dataModel = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
            isShowingAd = if (appOpenAd != null) {
                appOpenAd?.show(activity)
                true
            } else {
                applovinOpenAd?.showAd(dataModel?.key_open_app)
                true
            }
        }
    }
}
