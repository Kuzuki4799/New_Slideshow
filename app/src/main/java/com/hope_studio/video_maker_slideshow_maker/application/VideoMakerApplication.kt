package com.hope_studio.video_maker_slideshow_maker.application

import android.util.Log
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.AppsFlyerLibCore
import com.hope_studio.video_maker_slideshow_maker.modules.audio_manager_v3.AudioManagerV3
import com.hope_studio.video_maker_slideshow_maker.modules.audio_manager_v3.AudioManagerV3Impl
import com.hope_studio.video_maker_slideshow_maker.modules.local_storage.LocalStorageData
import com.hope_studio.video_maker_slideshow_maker.modules.local_storage.LocalStorageDataImpl
import com.hope_studio.video_maker_slideshow_maker.modules.music_player.MusicPlayer
import com.hope_studio.video_maker_slideshow_maker.modules.music_player.MusicPlayerImpl
import com.hope_studio.video_maker_slideshow_maker.ui.pick_media.PickMediaViewModelFactory
import com.hope_studio.video_maker_slideshow_maker.ui.select_music.SelectMusicViewModelFactory
import com.hope_studio.video_maker_slideshow_maker.ui.slide_show.SlideShowViewModelFactory
import com.hope_studio.base_ads.base.BaseApplication
import com.onesignal.OneSignal
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class VideoMakerApplication : BaseApplication(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@VideoMakerApplication))

        bind<LocalStorageData>() with singleton { LocalStorageDataImpl() }
        bind() from provider { PickMediaViewModelFactory(instance()) }
        bind() from provider { SelectMusicViewModelFactory(instance()) }
        bind() from provider { SlideShowViewModelFactory() }
        bind<AudioManagerV3>() with provider { AudioManagerV3Impl() }
        bind<MusicPlayer>() with provider { MusicPlayerImpl() }
    }

    companion object {
        lateinit var instance: VideoMakerApplication
        fun getContext() = instance.applicationContext!!
    }

    private val ONESIGNAL_APP_ID = "21301625-b075-4d02-9c76-5e926891ce46"

    private val AF_DEV_KEY = "R3wVtnhUDgAqNym872cHbh"

    override fun onCreate() {
        super.onCreate()
        instance = this


        val conversionListener: AppsFlyerConversionListener = object : AppsFlyerConversionListener {
            /* Returns the attribution data. Note - the same conversion data is returned every time per install */
            override fun onConversionDataSuccess(conversionData: Map<String, Any>) {
                for (attrName in conversionData.keys) {
                    Log.d(
                        AppsFlyerLibCore.LOG_TAG,
                        "attribute: " + attrName + " = " + conversionData[attrName]
                    )
                }
                setInstallData(conversionData)
            }

            override fun onConversionDataFail(errorMessage: String) {
                Log.d(AppsFlyerLibCore.LOG_TAG, "error getting conversion data: $errorMessage")
            }

            /* Called only when a Deep Link is opened */
            override fun onAppOpenAttribution(conversionData: Map<String, String>) {
                for (attrName in conversionData.keys) {
                    Log.d(
                        AppsFlyerLibCore.LOG_TAG,
                        "attribute: " + attrName + " = " + conversionData[attrName]
                    )
                }
            }

            override fun onAttributionFailure(errorMessage: String) {
                Log.d(AppsFlyerLibCore.LOG_TAG, "error onAttributionFailure : $errorMessage")
            }
        }

        /* This API enables AppsFlyer to detect installations, sessions, and updates. */

        AppsFlyerLib.getInstance().init(AF_DEV_KEY, conversionListener, applicationContext)
        AppsFlyerLib.getInstance().startTracking(this)

        /* Set to true to see the debug logs. Comment out or set to false to stop the function */
        AppsFlyerLib.getInstance().setDebugLog(true)


        // OneSignal
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)

        // promptForPushNotifications will show the native Android notification permission prompt.
        // We recommend removing the following code and instead using an In-App Message to prompt for notification permission (See step 7)
        OneSignal.promptForPushNotifications()
    }


    /* IGNORE - USED TO DISPLAY INSTALL DATA */
    var installConversionData = ""
    var sessionCount = 0
    fun setInstallData(conversionData: Map<String, Any>) {
        if (sessionCount == 0) {
            val installType = """
            Install Type: ${conversionData["af_status"]}
            
            """.trimIndent()
            val mediaSource = """
            Media Source: ${conversionData["media_source"]}
            
            """.trimIndent()
            val installTime = """
            Install Time(GMT): ${conversionData["install_time"]}
            
            """.trimIndent()
            val clickTime = """
            Click Time(GMT): ${conversionData["click_time"]}
            
            """.trimIndent()
            val isFirstLaunch = """
            Is First Launch: ${conversionData["is_first_launch"]}
            
            """.trimIndent()
            installConversionData += installType + mediaSource + installTime + clickTime + isFirstLaunch
            sessionCount++
        }
    }
}