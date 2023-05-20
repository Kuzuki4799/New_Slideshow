package com.hope_studio.video_maker_slideshow_maker.ho_application

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

    override fun onCreate() {
        super.onCreate()
        instance = this

        // OneSignal
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)

        // promptForPushNotifications will show the native Android notification permission prompt.
        // We recommend removing the following code and instead using an In-App Message to prompt for notification permission (See step 7)
        OneSignal.promptForPushNotifications()
    }
}