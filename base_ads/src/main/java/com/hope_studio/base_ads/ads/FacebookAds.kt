package com.hope_studio.base_ads.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.LinearLayout
import com.facebook.ads.*
import com.hope_studio.base_ads.ads.widget.NativeBannerAds
import com.hope_studio.base_ads.base.BaseActivity
import com.hope_studio.base_ads.model.DataModel
import com.hope_studio.base_ads.utils.ShareUtils

object FacebookAds {

    private var nativeAdsManager: NativeAdsManager? = null

    private var interstitialAd: InterstitialAd? = null
    private var rewardedVideoAd: RewardedVideoAd? = null
    private var rewardedInterstitialVideoAd: RewardedInterstitialAd? = null

    fun resetData() {
        nativeAdsManager = null
        interstitialAd = null
        rewardedVideoAd = null
    }

    fun getRewardedFacebookAd(): RewardedVideoAd? {
        return rewardedVideoAd
    }

    fun getRewardedInterstitialFacebookAd(): RewardedInterstitialAd? {
        return rewardedInterstitialVideoAd
    }

    fun getInterstitialFacebookAd(): InterstitialAd? {
        return interstitialAd
    }

    fun loadBannerFacebookAds(
        activity: BaseActivity, id: String, view: LinearLayout,
        onBannerAdsCallback: BaseAds.OnBannerAdsCallback
    ) {
        if (id.isEmpty()) {
            onBannerAdsCallback.onBannerLoadFail()
            return
        }
        val adView = AdView(activity, id, AdSize.BANNER_HEIGHT_50)
        val adListener: AdListener = object : AdListener {
            override fun onError(ad: Ad, adError: AdError) {
                if (BuildConfig.DEBUG) {
                    val errMessage =
                        "Banner Facebook Error: ${adError.errorMessage} - code + ${adError.errorCode}}"
                    Log.d("base_main_ads", errMessage)
                }
                onBannerAdsCallback.onBannerLoadFail()
            }

            override fun onAdLoaded(ad: Ad) {
                if (BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Banner Facebook Load")
                }
                view.removeAllViews()
                view.addView(adView)
                onBannerAdsCallback.onBannerLoadSuccess()
            }

            override fun onAdClicked(ad: Ad) {}
            override fun onLoggingImpression(ad: Ad) {}
        }
        val loadAdConfig = adView.buildLoadAdConfig().withAdListener(adListener).build()
        adView.loadAd(loadAdConfig)
    }

    fun initBaseNativeBannerFacebookAds(
        activity: Context, id: String, nativeBannerAds: NativeBannerAds
    ) {
        val nativeBannerLoader = NativeBannerAd(activity, id)
        nativeBannerLoader.loadAd(
            nativeBannerLoader.buildLoadAdConfig()
                ?.withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL)
                ?.withPreloadedIconView(100, 100)
                ?.withAdListener(object : NativeAdListener {
                    override fun onError(p0: Ad?, p1: AdError?) {
                        if (BuildConfig.DEBUG) {
                            val errMessage =
                                "Native Banner Facebook Error: ${p1?.errorMessage} - code + ${p1?.errorCode}}"
                            Log.d("base_main_ads", errMessage)
                        }
                    }

                    override fun onAdLoaded(p0: Ad?) {
                        nativeBannerAds.setFacebookNativeAd(nativeBannerLoader)
                        if (BuildConfig.DEBUG) {
                            Log.d("base_main_ads", "Native Banner Facebook Load")
                        }
                    }

                    override fun onAdClicked(p0: Ad?) {
                    }

                    override fun onLoggingImpression(p0: Ad?) {
                    }

                    override fun onMediaDownloaded(p0: Ad?) {
                    }
                })?.build()
        )
    }

    fun initBaseNativeBannerFacebookAds(
        activity: Context, id: String,
        onNativeAdCallback: BaseAds.OnNativeAdCallback<NativeBannerAd>
    ) {
        val nativeBannerLoader = NativeBannerAd(activity, id)
        nativeBannerLoader.loadAd(
            nativeBannerLoader.buildLoadAdConfig()
                ?.withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL)
                ?.withPreloadedIconView(100, 100)
                ?.withAdListener(object : NativeAdListener {
                    override fun onError(p0: Ad?, p1: AdError?) {
                        onNativeAdCallback.onNativeLoadFail()
                        if (BuildConfig.DEBUG) {
                            val errMessage =
                                "Native Banner Facebook Error: ${p1?.errorMessage} - code + ${p1?.errorCode}}"
                            Log.d("base_main_ads", errMessage)
                        }
                    }

                    override fun onAdLoaded(p0: Ad?) {
                        onNativeAdCallback.onNativeLoadSuccess(nativeBannerLoader)
                        if (BuildConfig.DEBUG) {
                            Log.d("base_main_ads", "Native Banner Facebook Load")
                        }
                    }

                    override fun onAdClicked(p0: Ad?) {
                    }

                    override fun onLoggingImpression(p0: Ad?) {
                    }

                    override fun onMediaDownloaded(p0: Ad?) {
                    }
                })?.build()
        )
    }

    fun setUpListNativeFacebookAds(
        activity: Context, id: String, onNativeAdCallback: BaseAds.OnNativeAdCallback<NativeAd>
    ) {
        val dataAds = ShareUtils[activity, DataModel::class.java.name, DataModel::class.java]
        if (dataAds == null) return
        if (!dataAds.getShowAds() || !dataAds.getShowNativeAds()) return

        if (nativeAdsManager == null) {
            nativeAdsManager = NativeAdsManager(activity, id, dataAds.getCountNative())
            nativeAdsManager?.setListener(object : NativeAdsManager.Listener {
                override fun onAdsLoaded() {
                    nativeAdsManager?.nextNativeAd()
                        ?.let { onNativeAdCallback.onNativeLoadSuccess(it) }
                    Log.d("base_main_ads", "Next Ads")
                    if (BuildConfig.DEBUG) {
                        Log.d("base_main_ads", "Native Facebook Load")
                    }
                }

                override fun onAdError(p0: AdError?) {
                    onNativeAdCallback.onNativeLoadFail()
                    if (BuildConfig.DEBUG) {
                        val errMessage =
                            "Native Facebook Error: ${p0?.errorMessage} - code + ${p0?.errorCode}}"
                        Log.d("base_main_ads", errMessage)
                    }
                }
            })
            nativeAdsManager?.loadAds()
        } else {
            if (nativeAdsManager?.uniqueNativeAdCount != 0) {
                nativeAdsManager?.nextNativeAd()?.let { onNativeAdCallback.onNativeLoadSuccess(it) }
                Log.d("base_main_ads", "Next Ads")
                if (BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Native Facebook Load")
                }
            } else {
                onNativeAdCallback.onNativeLoadFail()
                if (BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Native Facebook Error")
                }
            }
        }
    }

    fun initBaseNativeFacebookAds(
        activity: Context, id: String, onNativeAdCallback: BaseAds.OnNativeAdCallback<NativeAd>
    ) {
        val nativeAd = NativeAd(activity, id)
        nativeAd.loadAd(
            nativeAd.buildLoadAdConfig()?.withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL)
                ?.withPreloadedIconView(100, 100)?.withAdListener(object : NativeAdListener {
                    override fun onError(p0: Ad?, p1: AdError?) {
                        onNativeAdCallback.onNativeLoadFail()
                        if (BuildConfig.DEBUG) {
                            val errMessage =
                                "Native Facebook Error: ${p1?.errorMessage} - code + ${p1?.errorCode}}"
                            Log.d("base_main_ads", errMessage)
                        }
                    }

                    override fun onAdLoaded(p0: Ad?) {
                        onNativeAdCallback.onNativeLoadSuccess(nativeAd)
                        if (BuildConfig.DEBUG) {
                            Log.d("base_main_ads", "Native Facebook Load")
                        }
                    }

                    override fun onAdClicked(p0: Ad?) {
                    }

                    override fun onLoggingImpression(p0: Ad?) {
                    }

                    override fun onMediaDownloaded(p0: Ad?) {
                    }
                })?.build()
        )
    }

    fun showInterstitialFacebookAds() {
        interstitialAd?.show()
    }

    fun loadInterstitialFacebookAds(
        activity: Activity, id: String, onInterstitialAdCallback: BaseAds.OnInterstitialAdCallback
    ) {
        interstitialAd = InterstitialAd(activity, id)
        val adListener: AbstractAdListener = object : AbstractAdListener() {
            override fun onError(ad: Ad, error: AdError) {
                super.onError(ad, error)
                onInterstitialAdCallback.onInterstitialLoadFail()
                if (BuildConfig.DEBUG) {
                    val errMessage =
                        "Interstitial Facebook Error: ${error.errorMessage} - code + ${error.errorCode}}"
                    Log.d("base_main_ads", errMessage)
                }
            }

            override fun onAdLoaded(ad: Ad) {
                super.onAdLoaded(ad)
                if (BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Interstitial Facebook Load")
                }
                onInterstitialAdCallback.onInterstitialLoadSuccess()
            }

            override fun onAdClicked(ad: Ad) {
                super.onAdClicked(ad)
            }

            override fun onInterstitialDisplayed(ad: Ad) {
                super.onInterstitialDisplayed(ad)
                BaseAds.isShowAds = true
                if (BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Interstitial Facebook Show")
                }
            }

            override fun onInterstitialDismissed(ad: Ad) {
                super.onInterstitialDismissed(ad)
                onInterstitialAdCallback.onInterstitialClose()
            }
        }
        val interstitialLoadAdConfig =
            interstitialAd?.buildLoadAdConfig()?.withAdListener(adListener)?.build()
        interstitialAd?.loadAd(interstitialLoadAdConfig)
    }

    fun loadRewardedFacebookAds(
        activity: Activity, id: String, onRewardAdCallback: BaseAds.OnRewardAdCallback
    ) {
        rewardedVideoAd = RewardedVideoAd(activity, id)
        val rewardedVideoAdListener: RewardedVideoAdListener = object : RewardedVideoAdListener {
            override fun onError(ad: Ad, error: AdError) {
                onRewardAdCallback.onRewardLoadFail()
                if (BuildConfig.DEBUG) {
                    val errMessage =
                        "Reward Facebook Error: ${error.errorMessage} - code + ${error.errorCode}}"
                    Log.d("base_main_ads", errMessage)
                }
            }

            override fun onAdLoaded(ad: Ad) {
                if (BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Reward Facebook Load")
                }
                onRewardAdCallback.onRewardLoadSuccess()
            }

            override fun onAdClicked(ad: Ad) {}

            override fun onLoggingImpression(ad: Ad) {
                BaseAds.isShowAds = true
                if (BuildConfig.DEBUG) {
                    Log.d("base_main_ads", "Reward Facebook Show")
                }
            }

            override fun onRewardedVideoCompleted() {}

            override fun onRewardedVideoClosed() {
                onRewardAdCallback.onRewardClose()
            }
        }
        rewardedVideoAd?.loadAd(
            rewardedVideoAd?.buildLoadAdConfig()?.withAdListener(rewardedVideoAdListener)?.build()
        )
    }

    private fun checkInterstitialRewardFacebook(): Boolean {
        return if (interstitialAd != null) {
            interstitialAd?.isAdLoaded == true
        } else {
            false
        }
    }

    fun loadRewardedInterstitialFacebookAds(
        activity: Activity, id: String, onRewardAdCallback: BaseAds.OnRewardAdCallback
    ) {
        rewardedInterstitialVideoAd = RewardedInterstitialAd(activity, id)
        val rewardedVideoAdListener: RewardedInterstitialAdListener =
            object : RewardedInterstitialAdListener {
                override fun onError(ad: Ad, error: AdError) {
                    onRewardAdCallback.onRewardLoadFail()
                    if (BuildConfig.DEBUG) {
                        val errMessage =
                            "Reward Interstitial Facebook Error: ${error.errorMessage} - code + ${error.errorCode}}"
                        Log.d("base_main_ads", errMessage)
                    }
                }

                override fun onAdLoaded(ad: Ad) {
                    if (BuildConfig.DEBUG) {
                        Log.d("base_main_ads", "Reward Interstitial Facebook Load")
                    }
                    onRewardAdCallback.onRewardLoadSuccess()
                }

                override fun onAdClicked(ad: Ad) {}

                override fun onLoggingImpression(ad: Ad) {
                    BaseAds.isShowAds = true
                    if (BuildConfig.DEBUG) {
                        Log.d("base_main_ads", "Reward Interstitial Facebook Show")
                    }
                }

                override fun onRewardedInterstitialCompleted() {
                }

                override fun onRewardedInterstitialClosed() {
                    onRewardAdCallback.onRewardClose()
                }
            }
        rewardedInterstitialVideoAd?.loadAd(
            rewardedInterstitialVideoAd?.buildLoadAdConfig()
                ?.withAdListener(rewardedVideoAdListener)?.build()
        )
    }
}