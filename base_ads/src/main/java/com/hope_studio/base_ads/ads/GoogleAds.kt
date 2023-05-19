package com.hope_studio.base_ads.ads

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.LinearLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.hope_studio.base_ads.BuildConfig
import com.hope_studio.base_ads.ads.widget.NativeBannerAds
import com.hope_studio.base_ads.base.BaseActivity
import com.hope_studio.base_ads.utils.AnalyticsUtils
import com.hope_studio.base_ads.widget.FrameAdsView
import java.lang.NullPointerException

object GoogleAds {

    private var nativeAdLoader: AdLoader? = null
    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    val listNative = ArrayList<NativeAd>()

    private var currentNative = -1
    private var nativeLoadSize = 0

    fun getRewardedGoogleAd(): RewardedAd? {
        return rewardedAd
    }

    fun getInterstitialGoogleAd(): InterstitialAd? {
        return interstitialAd
    }

    fun resetData() {
        rewardedAd = null
        interstitialAd = null
        listNative.clear()
        currentNative = -1
        nativeLoadSize = 0
        nativeAdLoader = null
    }

    fun initAppId(context: Context, id: String) {
        if (id.isEmpty()) return
        try {
            val ai: ApplicationInfo = context.packageManager.getApplicationInfo(
                context.packageName, PackageManager.GET_META_DATA
            )
            val bundle: Bundle = ai.metaData
            ai.metaData.putString("com.google.android.gms.ads.APPLICATION_ID", id)
            val apiKey: String = bundle.getString("com.google.android.gms.ads.APPLICATION_ID")!!
            Log.d("base_main_ads", "ReNamed Found: $apiKey")
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("base_main_ads", "Failed to load meta-data, NameNotFound: " + e.message)
        } catch (e: NullPointerException) {
            Log.e("base_main_ads", "Failed to load meta-data, NullPointer: " + e.message)
        }
    }

    private fun adSize(activity: Activity, view: LinearLayout): AdSize {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = view.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    fun loadBannerGoogleAds(
        context: Activity, id: String, view: LinearLayout,
        onBannerAdsCallback: BaseAds.OnBannerAdsCallback
    ) {
        if (id.isEmpty()) {
            onBannerAdsCallback.onBannerLoadFail()
            return
        }
        val mAdView = AdView(context)
        mAdView.adSize = adSize(context, view)
        mAdView.adUnitId = id
        mAdView.loadAd(AdRequest.Builder().build())

        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                view.removeAllViews()
                if (!BaseAds.preloadInterstitial(context)) {
                    view.addView(FrameAdsView(context))
                }
                view.addView(mAdView)
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Banner Google Load")
                }
                onBannerAdsCallback.onBannerLoadSuccess()
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    val errMessage = "Banner Google Error: ${p0.message} - code + ${p0.code}"
                    Log.d("base_main_ads", errMessage)
                }
                onBannerAdsCallback.onBannerLoadFail()
            }

            override fun onAdClicked() {
                super.onAdClicked()
                val bundle = Bundle()
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    val errMessage = "Banner Google Click: ${context::class.java.simpleName}"
                    Log.d("base_main_ads", errMessage)
                }
                bundle.putString("class_name", context::class.java.simpleName)
                AnalyticsUtils.pushEventAnalytic("admob_banner_click", null)
            }
        }
    }

    fun initBaseNativeBannerAds(context: Context, id: String, nativeBannerAds: NativeBannerAds) {
        val adLoader = AdLoader.Builder(context, id)
            .forNativeAd { nativeAd -> nativeBannerAds.setGoogleNativeAd(nativeAd) }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                        Log.d("base_main_ads", "Native Banner Google Load")
                    }
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                        val errMessage =
                            "Native Banner Google Error: ${p0.message} - code + ${p0.code}"
                        Log.d("base_main_ads", errMessage)
                    }
                }
            }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun initBaseNativeBannerAds(
        context: Context, id: String, onNativeAdCallback: BaseAds.OnNativeAdCallback<NativeAd>
    ) {
        val adLoader = AdLoader.Builder(context, id)
            .forNativeAd { nativeAd -> onNativeAdCallback.onNativeLoadSuccess(nativeAd) }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                        Log.d("base_main_ads", "Native Banner Google Load")
                    }
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    onNativeAdCallback.onNativeLoadFail()
                    if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                        val errMessage =
                            "Native Banner Google Error: ${p0.message} - code + ${p0.code}"
                        Log.d("base_main_ads", errMessage)
                    }
                }
            }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun getNative(): NativeAd {
        if (currentNative == listNative.size - 1) {
            currentNative = 0
        } else {
            currentNative += 1
        }
        if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
            Log.d("base_main_ads", "Native Next $currentNative")
        }
        return listNative[currentNative]
    }

    fun loadNativeAds(
        activity: Context, id: String, count: Int, onNativeAdCallback: BaseAds.OnNativeCallback
    ) {
        nativeAdLoader = AdLoader.Builder(activity, id)
            .forNativeAd { nativeAd: NativeAd ->
                nativeLoadSize += 1
                if (nativeAdLoader?.isLoading == true) {
                    listNative.add(nativeAd)
                    if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                        Log.d("base_main_ads", "Native Google Load ${listNative.size}")
                    }
                }

                if (nativeLoadSize == count) {
                    onNativeAdCallback.onNativeSuccess()
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    if (listNative.size != 0) {
                        onNativeAdCallback.onNativeSuccess()
                    } else {
                        onNativeAdCallback.onNativeFail()
                        if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                            val errMessage =
                                "Native Google Error: ${p0.message} - code + ${p0.code}"
                            Log.d("base_main_ads", errMessage)
                        }
                    }
                }
            })
            .build()
        nativeAdLoader?.loadAds(AdRequest.Builder().build(), count)
    }

    fun initBaseNativeAds(
        context: Context, id: String, onNativeAdCallback: BaseAds.OnNativeAdCallback<NativeAd>
    ) {
        val adLoader = AdLoader.Builder(context, id)
            .forNativeAd { nativeAd -> onNativeAdCallback.onNativeLoadSuccess(nativeAd) }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                        Log.d("base_main_ads", "Native Google Load")
                    }
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    onNativeAdCallback.onNativeLoadFail()
                    if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                        val errMessage =
                            "Native Google Error: ${p0.message} - code + ${p0.code}"
                        Log.d("base_main_ads", errMessage)
                    }
                }
            }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun showInterstitialGoogleAds(activity: BaseActivity) {
        interstitialAd?.show(activity)
    }

    fun loadInterstitialGoogleAds(
        activity: Activity, id: String, onInterstitialAdCallback: BaseAds.OnInterstitialAdCallback
    ) {
        try {
            activity.runOnUiThread {
                InterstitialAd.load(
                    activity, id, AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            this@GoogleAds.interstitialAd = null
                            onInterstitialAdCallback.onInterstitialLoadFail()
                            if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                                val errMessage =
                                    "Interstitial Google Error: ${adError.message} - code + ${adError.code}"
                                Log.d("base_main_ads", errMessage)
                            }
                        }

                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            this@GoogleAds.interstitialAd = interstitialAd
                            if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                                Log.d("base_main_ads", "Interstitial Google Load")
                            }
                            setInterstitialAdFullScreen(onInterstitialAdCallback)
                            onInterstitialAdCallback.onInterstitialLoadSuccess()
                        }
                    }
                )
            }
        } catch (e: Exception) {
            e.stackTrace
        }
    }

    fun setInterstitialAdFullScreen(onInterstitialAdCallback: BaseAds.OnInterstitialAdCallback) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                onInterstitialAdCallback.onInterstitialClose()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Interstitial Google Show Fail")
                }
                interstitialAd = null
                onInterstitialAdCallback.onInterstitialShowFail()
            }

            override fun onAdShowedFullScreenContent() {
                BaseAds.isShowAds = true
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Interstitial Google Show")
                }
            }
        }
    }

    fun showRewardGoogleAds(activity: Activity) {
        rewardedAd?.show(activity) { p0 ->
            if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                Log.d("base_main_ads", "Amount: ${p0.amount} - type: ${p0.type}")
            }
        }
    }

    fun loadRewardedGoogleAds(
        activity: Activity, id: String, onRewardAdCallback: BaseAds.OnRewardAdCallback
    ) {
        try {
            activity.runOnUiThread {
                RewardedAd.load(
                    activity, id, AdRequest.Builder().build(),
                    object : RewardedAdLoadCallback() {
                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            rewardedAd = null
                            onRewardAdCallback.onRewardLoadFail()
                            if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                                val errMessage =
                                    "Reward Google Error: ${p0.message} - code + ${p0.code}"
                                Log.d("base_main_ads", errMessage)
                            }
                        }

                        override fun onAdLoaded(p0: RewardedAd) {
                            super.onAdLoaded(p0)
                            rewardedAd = p0
                            setRewardedAdFullScreen(onRewardAdCallback)
                            if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                                Log.d("base_main_ads", "Reward Google Load")
                            }
                            onRewardAdCallback.onRewardLoadSuccess()
                        }
                    })
            }
        } catch (e: Exception) {
            e.stackTrace
        }
    }

    fun setRewardedAdFullScreen(onRewardAdCallback: BaseAds.OnRewardAdCallback) {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                onRewardAdCallback.onRewardClose()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                onRewardAdCallback.onRewardShowFail()
            }

            override fun onAdShowedFullScreenContent() {
                BaseAds.isShowAds = true
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Reward Google Show")
                }
            }
        }
    }
}