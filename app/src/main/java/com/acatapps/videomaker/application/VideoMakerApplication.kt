package com.acatapps.videomaker.application

import com.acatapps.videomaker.modules.audio_manager_v3.AudioManagerV3
import com.acatapps.videomaker.modules.audio_manager_v3.AudioManagerV3Impl
import com.acatapps.videomaker.modules.local_storage.LocalStorageData
import com.acatapps.videomaker.modules.local_storage.LocalStorageDataImpl
import com.acatapps.videomaker.modules.music_player.MusicPlayer
import com.acatapps.videomaker.modules.music_player.MusicPlayerImpl
import com.acatapps.videomaker.ui.pick_media.PickMediaViewModelFactory
import com.acatapps.videomaker.ui.select_music.SelectMusicViewModelFactory
import com.acatapps.videomaker.ui.slide_show.SlideShowViewModelFactory
import com.hope_studio.base_ads.base.BaseApplication
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

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}