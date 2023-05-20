package com.hope_studio.video_maker_slideshow_maker.ho_modules.ho_local_storage

import androidx.lifecycle.MutableLiveData
import com.hope_studio.video_maker_slideshow_maker.ho_data.AudioData
import com.hope_studio.video_maker_slideshow_maker.ho_data.MediaData
import com.hope_studio.video_maker_slideshow_maker.ho_enum_.MediaKind

interface LocalStorageData {
    val audioDataResponse:MutableLiveData<ArrayList<AudioData>>
    val mediaDataResponse:MutableLiveData<ArrayList<MediaData>>

    fun getAllAudio()
    fun getAllMedia(mediaKind: MediaKind)
}