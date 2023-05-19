package com.hope_studio.base_ads.ads

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.applovin.mediation.*
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkUtils
import com.hope_studio.base_ads.BuildConfig
import com.hope_studio.base_ads.model.DataModel
import com.hope_studio.base_ads.widget.FrameAdsView

object ApplovinAd {

    private var interstitialAd: MaxInterstitialAd? = null
    private var rewardAd: MaxRewardedAd? = null

    fun resetData() {
        interstitialAd = null
        rewardAd = null
    }

    fun initAppId(context: Context, id: String) {
        if (id.isEmpty()) return
        try {
            val ai: ApplicationInfo = context.packageManager.getApplicationInfo(
                context.packageName, PackageManager.GET_META_DATA
            )
            val bundle: Bundle = ai.metaData
            ai.metaData.putString("applovin.sdk.key", id)
            val apiKey: String = bundle.getString("applovin.sdk.key")!!
            Log.d("base_main_ads", "ReNamed Found: $apiKey")

            AppLovinSdk.getInstance(context).mediationProvider = AppLovinMediationProvider.MAX
            AppLovinSdk.getInstance(context).initializeSdk {
                Log.d("base_main_ads", "AppLovinSdk initializeSdk")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d("base", "Failed to load meta-data, NameNotFound: " + e.message)
        } catch (e: NullPointerException) {
            Log.d("base_main_ads", "Failed to load meta-data, NullPointer: " + e.message)
        }
    }

    fun getInterstitial(): MaxInterstitialAd? {
        return interstitialAd
    }

    fun getReward(): MaxRewardedAd? {
        return rewardAd
    }

    fun initAppId(activity: Activity) {
        AppLovinSdk.getInstance(activity).mediationProvider = AppLovinMediationProvider.MAX
        AppLovinSdk.getInstance(activity).initializeSdk {}
    }

    fun loadBannerApplovinAds(
        activity: Context, id: String, frameLayout: LinearLayout,
        onBannerAdsCallback: BaseAds.OnBannerAdsCallback
    ) {
        if (id.isEmpty()) {
            onBannerAdsCallback.onBannerLoadFail()
            return
        }

        val adView = MaxAdView(id, activity)
        val isTablet = AppLovinSdkUtils.isTablet(activity)
        val heightPx = AppLovinSdkUtils.dpToPx(activity, if (isTablet) 90 else 50)
        adView.layoutParams =
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx)
        adView.setExtraParameter("adaptive_banner", "true")
//        adView.setExtraParameter( "allow_pause_auto_refresh_immediately", "true" )
//        adView.stopAutoRefresh()
        adView.setListener(object : MaxAdViewAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                frameLayout.removeAllViews()
                if (!BaseAds.getPreLoad(activity as Activity)) {
                    frameLayout.addView(FrameAdsView(activity))
                }
                frameLayout.addView(adView)
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d(
                        "base_main_ads",
                        "Banner Applovin Load ${(activity as Activity).localClassName}"
                    )
                }
            }

            override fun onAdDisplayed(ad: MaxAd?) {
            }

            override fun onAdHidden(ad: MaxAd?) {
            }

            override fun onAdClicked(ad: MaxAd?) {
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Banner Applovin Error: ${error?.message}")
                }
                onBannerAdsCallback.onBannerLoadFail()
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Banner Applovin Error: ${error?.message}")
                }
                onBannerAdsCallback.onBannerLoadFail()
            }

            override fun onAdExpanded(ad: MaxAd?) {
            }

            override fun onAdCollapsed(ad: MaxAd?) {
            }
        })
        adView.loadAd()
    }

    fun initBaseNativeBannerAds(
        context: Context, id: String, width: Int,
        onNativeAdCallback: BaseAds.OnNativeAdCallback<MaxAdView>
    ) {
        val adView = MaxAdView(id, context)
        adView.layoutParams = LinearLayout.LayoutParams(
            width, ((width / 4) * 1)
        )
        adView.setListener(object : MaxAdViewAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                onNativeAdCallback.onNativeLoadSuccess(adView)
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Native Banner Applovin Load")
                }
            }

            override fun onAdDisplayed(ad: MaxAd?) {
            }

            override fun onAdHidden(ad: MaxAd?) {
            }

            override fun onAdClicked(ad: MaxAd?) {
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                onNativeAdCallback.onNativeLoadFail()
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d(
                        "base_main_ads",
                        "Native Banner Applovin Error: ${error!!.code} - ${error.message}"
                    )
                }
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                onNativeAdCallback.onNativeLoadFail()
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d(
                        "base_main_ads",
                        "Native Banner Applovin Error: ${error!!.code} - ${error.message}"
                    )
                }
            }

            override fun onAdExpanded(ad: MaxAd?) {
            }

            override fun onAdCollapsed(ad: MaxAd?) {
            }
        })
        adView.loadAd()
    }

    fun initBaseNativeAds(
        context: Context, id: String, width: Int, data: DataModel,
        onNativeAdCallback: BaseAds.OnNativeAdCallback2<MaxAdView?, MaxNativeAdView?>
    ) {
        if (data.getBannerOrNative()) {
            val adView = MaxAdView(id, context)
            adView.layoutParams = LinearLayout.LayoutParams(
                width, AppLovinSdkUtils.dpToPx(context, 250)
            )
            adView.setListener(object : MaxAdViewAdListener {
                override fun onAdLoaded(ad: MaxAd?) {
                    onNativeAdCallback.onNativeLoadSuccess(adView, null)
                    if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                        Log.d("base_main_ads", "Native  Applovin Load")
                    }
                }

                override fun onAdDisplayed(ad: MaxAd?) {
                }

                override fun onAdHidden(ad: MaxAd?) {
                }

                override fun onAdClicked(ad: MaxAd?) {
                }

                override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                    onNativeAdCallback.onNativeLoadFail()
                    if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                        Log.d(
                            "base_main_ads",
                            "Native  Applovin Error: ${error!!.code} - ${error.message}"
                        )
                    }
                }

                override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                    onNativeAdCallback.onNativeLoadFail()
                    if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                        Log.d(
                            "base_main_ads",
                            "Native Banner Applovin Error: ${error!!.code} - ${error.message}"
                        )
                    }
                }

                override fun onAdExpanded(ad: MaxAd?) {
                }

                override fun onAdCollapsed(ad: MaxAd?) {
                }
            })
            adView.loadAd()
        } else {
            val nativeAdLoader = MaxNativeAdLoader(id, context)
            nativeAdLoader.setNativeAdListener(object : MaxNativeAdListener() {
                override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd?) {
                    nativeAdView?.let { onNativeAdCallback.onNativeLoadSuccess(null, it) }
                    if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                        Log.d("base_main_ads", "Native Applovin Load")
                    }
                }

                override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                    onNativeAdCallback.onNativeLoadFail()
                    if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                        Log.d(
                            "base_main_ads",
                            "Native Applovin Error: ${error.code} - ${error.message}"
                        )
                    }
                }

                override fun onNativeAdClicked(ad: MaxAd) {
                }
            })
            nativeAdLoader.loadAd()
        }
    }

    fun loadInterstitialApplovinAd(
        activity: Activity, id: String, onInterstitialAdCallback: BaseAds.OnInterstitialAdCallback
    ) {
        interstitialAd = MaxInterstitialAd(id, activity)
        interstitialAd?.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                onInterstitialAdCallback.onInterstitialLoadSuccess()
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Interstitial Applovin Load")
                }
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                BaseAds.isShowAds = true
            }

            override fun onAdHidden(ad: MaxAd?) {
                onInterstitialAdCallback.onInterstitialClose()
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Interstitial Applovin Hidden")
                }
            }

            override fun onAdClicked(ad: MaxAd?) {
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                onInterstitialAdCallback.onInterstitialLoadFail()
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Interstitial Applovin Error: ${error?.message}")
                }
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                onInterstitialAdCallback.onInterstitialShowFail()
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Interstitial Applovin Error: ${error?.message}")
                }
            }
        })
        interstitialAd?.loadAd()
    }

    fun loadRewardApplovinAds(
        activity: Activity, id: String, onRewardAdCallback: BaseAds.OnRewardAdCallback
    ) {
        rewardAd = MaxRewardedAd.getInstance(id, activity)
        rewardAd?.setListener(object : MaxRewardedAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                onRewardAdCallback.onRewardLoadSuccess()
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Reward Applovin Load")
                }
            }

            override fun onAdDisplayed(ad: MaxAd?) {
            }

            override fun onAdHidden(ad: MaxAd?) {
            }

            override fun onAdClicked(ad: MaxAd?) {
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                onRewardAdCallback.onRewardLoadFail()
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d(
                        "base_main_ads",
                        "Reward Applovin Error: ${error?.code} - ${error?.message}"
                    )
                }
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                onRewardAdCallback.onRewardShowFail()
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d(
                        "base_main_ads",
                        "Reward Applovin Error: ${error?.code} - ${error?.message}"
                    )
                }
            }

            override fun onRewardedVideoStarted(ad: MaxAd?) {
            }

            override fun onRewardedVideoCompleted(ad: MaxAd?) {
                onRewardAdCallback.onRewardClose()
                if (com.hope_studio.base_ads.BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Reward Applovin Hidden")
                }
            }

            override fun onUserRewarded(ad: MaxAd?, reward: MaxReward?) {
            }
        })
        rewardAd?.loadAd()
    }
}