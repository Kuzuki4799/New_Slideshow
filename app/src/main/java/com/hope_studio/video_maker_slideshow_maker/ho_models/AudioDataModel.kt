package com.hope_studio.video_maker_slideshow_maker.ho_models

import com.hope_studio.video_maker_slideshow_maker.ho_data.AudioData
import com.hope_studio.video_maker_slideshow_maker.ho_utils.Utils

class AudioDataModel(private val audioData: AudioData) {

    val audioName:String
    get() = audioData.musicName

    val durationString:String
    get() = Utils.convertSecToTimeString(audioData.duration.toInt()/1000)

    val duration:Long
    get() = audioData.duration

    var isSelect = false

    val audioFilePath:String
    get() = audioData.filePath

    var startOffset = 0
    var length = duration

    var isPlaying = false

    val fileType:String = audioData.mineType



    fun reset() {
        startOffset = 0
        length =  duration
    }

}