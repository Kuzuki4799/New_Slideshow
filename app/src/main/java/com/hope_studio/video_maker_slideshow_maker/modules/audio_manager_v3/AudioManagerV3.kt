package com.hope_studio.video_maker_slideshow_maker.modules.audio_manager_v3

import com.hope_studio.video_maker_slideshow_maker.data.MusicReturnData

interface AudioManagerV3 {

    fun getAudioName():String
    fun playAudio()
    fun pauseAudio()
    fun returnToDefault(currentTimeMs: Int)
    fun seekTo(currentTimeMs:Int)
    fun repeat()
    fun setVolume(volume:Float)
    fun getVolume():Float
    fun changeAudio(musicReturnData: MusicReturnData, currentTimeMs: Int)
    fun changeMusic(path:String)
    fun getOutMusicPath():String
    fun getOutMusic():MusicReturnData

    fun useDefault()
}