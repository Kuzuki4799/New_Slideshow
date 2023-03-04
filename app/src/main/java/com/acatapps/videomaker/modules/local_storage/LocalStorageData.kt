package com.acatapps.videomaker.modules.local_storage

import androidx.lifecycle.MutableLiveData
import com.acatapps.videomaker.data.AudioData
import com.acatapps.videomaker.data.ImageData
import com.acatapps.videomaker.data.MediaData
import com.acatapps.videomaker.data.VideoData
import com.acatapps.videomaker.enum_.MediaKind

interface LocalStorageData {
    val audioDataResponse:MutableLiveData<ArrayList<AudioData>>
    val mediaDataResponse:MutableLiveData<ArrayList<MediaData>>

    fun getAllAudio()
    fun getAllMedia(mediaKind: MediaKind)
}