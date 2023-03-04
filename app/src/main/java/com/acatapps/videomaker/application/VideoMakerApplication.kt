package com.acatapps.videomaker.application

import android.app.Application
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.acatapps.videomaker.R
import com.acatapps.videomaker.broadcast.InternetStateChange
import com.acatapps.videomaker.modules.audio_manager_v3.AudioManagerV3
import com.acatapps.videomaker.modules.audio_manager_v3.AudioManagerV3Impl
import com.acatapps.videomaker.modules.local_storage.LocalStorageData
import com.acatapps.videomaker.modules.local_storage.LocalStorageDataImpl
import com.acatapps.videomaker.modules.music_player.MusicPlayer
import com.acatapps.videomaker.modules.music_player.MusicPlayerImpl
import com.acatapps.videomaker.ui.pick_media.PickMediaViewModelFactory
import com.acatapps.videomaker.ui.select_music.SelectMusicViewModelFactory
import com.acatapps.videomaker.ui.slide_show.SlideShowViewModelFactory
import com.acatapps.videomaker.utils.Logger
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class VideoMakerApplication : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@VideoMakerApplication))

        bind<LocalStorageData>() with singleton { LocalStorageDataImpl() }
        bind() from provider { PickMediaViewModelFactory(instance()) }
        bind() from provider { SelectMusicViewModelFactory(instance()) }
        bind() from provider { SlideShowViewModelFactory() }
        bind<AudioManagerV3>() with  provider { AudioManagerV3Impl() }
        bind<MusicPlayer>() with  provider { MusicPlayerImpl() }
    }

    private val mHomeInterstitialAd by lazy {
        InterstitialAd(this)
    }
    private val mInterstitialAd by lazy {
        InterstitialAd(this)
    }

    private var nativeAd: UnifiedNativeAd? = null
    private var mAdLoader: AdLoader? = null
    companion object {
        lateinit var instance: VideoMakerApplication
        fun getContext() = instance.applicationContext!!
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        MobileAds.initialize(this)
        MobileAds.setAppMuted(true)

        initInterstitialAd()
        initNativeAds()
        loadAd()


        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(InternetStateChange(), filter)
    }

    fun loadAd() {
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdFailedToLoad(p0: Int) {
            }

            override fun onAdClosed() {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
        }
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        mHomeInterstitialAd.loadAd(AdRequest.Builder().build())
      //  mAdLoader?.loadAd(AdRequest.Builder().build())
    }

    private fun initInterstitialAd() {
        mHomeInterstitialAd.apply {
            adUnitId = getString(R.string.full_open_app_id)
            loadAd(AdRequest.Builder().build())
            adListener = object : AdListener() {
                override fun onAdFailedToLoad(p0: Int) {
                    Logger.e("load home ad fail $p0")
                }

                override fun onAdClosed() {
                    mHomeInterstitialAd.loadAd(AdRequest.Builder().build())
                }
            }
        }

        mInterstitialAd.apply {
            adUnitId = getString(R.string.full_ads_id)
            loadAd(AdRequest.Builder().build())
            adListener = object : AdListener() {
                override fun onAdFailedToLoad(p0: Int) {

                }

                override fun onAdClosed() {
                    mInterstitialAd.loadAd(AdRequest.Builder().build())
                }
            }
        }

    }

    fun showInterHome(onClose:(()->Unit)?=null):Boolean {
        if (mHomeInterstitialAd.isLoaded) {
            mHomeInterstitialAd.show()

            mHomeInterstitialAd.adListener =object : AdListener() {
                override fun onAdFailedToLoad(p0: Int) {
                }

                override fun onAdClosed() {
                    onClose?.invoke()
                    loadAd()
                }
            }
            return true
        } else {
            loadAd()
            return false
        }
    }

    fun showAdsFull() {
        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.adListener
            mInterstitialAd.show()
        } else {
            loadAd()
        }
    }
    fun showAdsFull(onClose: (() -> Unit)?):Boolean {
        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.adListener = object : AdListener() {
                override fun onAdFailedToLoad(p0: Int) {

                }

                override fun onAdClosed() {
                    onClose?.invoke()
                    loadAd()
                }
            }
            mInterstitialAd.show()
            return true
        } else {
            loadAd()
            return false
        }
    }



    private fun initNativeAds() {
        mAdLoader = AdLoader.Builder(this, getString(R.string.ads_native))
            .forUnifiedNativeAd { ad: UnifiedNativeAd ->
                nativeAd?.destroy()
                nativeAd = ad
            }
            .withAdListener(object : AdListener() {

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Logger.e("native ad loaded")
                }

                override fun onAdFailedToLoad(p0: Int) {
                    super.onAdFailedToLoad(p0)
                    Logger.e("native ad load failed --> $p0")
                    nativeAd = null
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    val request = AdRequest.Builder().build()
                    mAdLoader?.loadAd(request)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        val request = AdRequest.Builder().build()
        mAdLoader?.loadAd(request)
    }
    fun getNativeAds() = nativeAd

    private var mRewardedVideoAd:RewardedVideoAd?=null
    var onRewardLoaded:(()->Unit)?=null
     fun loadRewardAd() {
        // if(mRewardedVideoAd != null) return
        val ad = MobileAds.getRewardedVideoAdInstance(this)
        ad.loadAd(getString(R.string.reward_ads_id), AdRequest.Builder().build())
        ad.rewardedVideoAdListener =object :RewardedVideoAdListener {
            override fun onRewardedVideoAdClosed() {
                Logger.e("onRewardedVideoAdClosed")
                mRewardedVideoAd = null
                 //loadRewardAd()
            }

            override fun onRewardedVideoAdLeftApplication() {
                Logger.e("onRewardedVideoAdLeftApplication")
            }

            override fun onRewardedVideoAdLoaded() {
                Logger.e("onRewardedVideoAdLoaded")
                onRewardLoaded?.invoke()
            }

            override fun onRewardedVideoAdOpened() {
                Logger.e("onRewardedVideoAdOpened")
            }

            override fun onRewardedVideoCompleted() {
                Logger.e("onRewardedVideoCompleted")
                // onComplete.invoke()

            }

            override fun onRewarded(p0: RewardItem?) {
                Logger.e("onRewarded $p0")
            }

            override fun onRewardedVideoStarted() {
                Logger.e("onRewardedVideoStarted")
            }

            override fun onRewardedVideoAdFailedToLoad(p0: Int) {
                Logger.e("onRewardedVideoAdFailedToLoad")
            }

        }
        mRewardedVideoAd = ad
    }
    fun loadRewardAd(onLoaded:()->Unit, onError:(()->Unit)?=null) {
        val ad = MobileAds.getRewardedVideoAdInstance(this)
        ad.loadAd(getString(R.string.reward_ads_id), AdRequest.Builder().build())

        ad.rewardedVideoAdListener =object :RewardedVideoAdListener {
            override fun onRewardedVideoAdClosed() {

            }

            override fun onRewardedVideoAdLeftApplication() {
                Logger.e("onRewardedVideoAdLeftApplication")
            }

            override fun onRewardedVideoAdLoaded() {
                Logger.e("onRewardedVideoAdLoaded")
                onLoaded.invoke()
            }

            override fun onRewardedVideoAdOpened() {
                Logger.e("onRewardedVideoAdOpened")
            }

            override fun onRewardedVideoCompleted() {
                Logger.e("onRewardedVideoCompleted")

            }

            override fun onRewarded(p0: RewardItem?) {
                Logger.e("onRewarded $p0")
            }

            override fun onRewardedVideoStarted() {
                Logger.e("onRewardedVideoStarted")
            }

            override fun onRewardedVideoAdFailedToLoad(p0: Int) {
                Logger.e("onRewardedVideoAdFailedToLoad = $p0")
                onError?.invoke()
            }

        }
        mRewardedVideoAd = ad
    }


    fun rewardAdReady():Boolean {
        if(mRewardedVideoAd?.isLoaded == true) return true
        return false
    }
    fun showRewardAd(onComplete:()->Unit):Boolean {
        mRewardedVideoAd?.let {
            it.rewardedVideoAdListener = object :RewardedVideoAdListener {
                override fun onRewardedVideoAdClosed() {
                    Logger.e("onRewardedVideoAdClosed")
                    mRewardedVideoAd = null
                }

                override fun onRewardedVideoAdLeftApplication() {
                    Logger.e("onRewardedVideoAdLeftApplication")
                }

                override fun onRewardedVideoAdLoaded() {
                    Logger.e("onRewardedVideoAdLoaded")
                    onRewardLoaded?.invoke()
                }

                override fun onRewardedVideoAdOpened() {
                    Logger.e("onRewardedVideoAdOpened")
                }

                override fun onRewardedVideoCompleted() {
                    Logger.e("onRewardedVideoCompleted")

                }

                override fun onRewarded(p0: RewardItem?) {
                    Logger.e("onRewarded $p0")
                    onComplete.invoke()
                }

                override fun onRewardedVideoStarted() {
                    Logger.e("onRewardedVideoStarted")
                }

                override fun onRewardedVideoAdFailedToLoad(p0: Int) {
                    Logger.e("onRewardedVideoAdFailedToLoad")
                }

            }

            if(it.isLoaded) {
                it.show()
                return true
            } else {

                return false
            }
        }
        if(mRewardedVideoAd == null) loadRewardAd()
        return false
    }
    fun releaseRewardAd() {

        mRewardedVideoAd?.destroy(this)
        mRewardedVideoAd = null
    }

    fun loadAdFullForTheme(onLoaded:()->Unit) {
        Logger.e("load ad for theme")
        val fullAd = InterstitialAd(this).apply {
            adUnitId =  getString(R.string.full_load_theme)
        }

        fullAd.adListener = object :AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                onLoaded.invoke()
                fullAd.show()
                Logger.e("load ad for theme onAdLoaded")
            }

            override fun onAdFailedToLoad(p0: Int) {
                super.onAdFailedToLoad(p0)
                Logger.e("load ad for theme onAdFailedToLoad = $p0")
                onLoaded.invoke()
            }
        }

        fullAd.loadAd(AdRequest.Builder().build())


    }

}