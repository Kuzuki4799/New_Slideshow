package com.acatapps.videomaker.video_player_slide

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.data.VideoInSlideData
import com.acatapps.videomaker.gs_effect.GSEffectUtils
import com.acatapps.videomaker.utils.RawResourceReader

class VideoPlayerSlideGLView (context: Context, attributes: AttributeSet?) : GLSurfaceView(context, attributes) {


    private lateinit var mVideoPlayerSlideRenderer: VideoPlayerSlideRenderer
    private var mCurrentVideoPath =""
    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
    }

    fun performSetRenderer(videoPlayerSlideRenderer: VideoPlayerSlideRenderer) {
        mVideoPlayerSlideRenderer = videoPlayerSlideRenderer
        setRenderer(mVideoPlayerSlideRenderer)
    }

    fun changeVideo(videoInSlideData: VideoInSlideData) {
        queueEvent(Runnable {
            mCurrentVideoPath = videoInSlideData.path
            mVideoPlayerSlideRenderer.changeVideo(videoInSlideData)
        })
    }

    fun seekTo(videoInSlideData: VideoInSlideData, timeMilSec:Int, autoPlay:Boolean=false) {
        queueEvent(Runnable {
            mCurrentVideoPath = videoInSlideData.path
            mVideoPlayerSlideRenderer.changeVideo(videoInSlideData, timeMilSec, autoPlay)
        })
    }

}