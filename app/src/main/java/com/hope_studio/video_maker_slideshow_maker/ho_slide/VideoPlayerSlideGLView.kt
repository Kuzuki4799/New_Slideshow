package com.hope_studio.video_maker_slideshow_maker.ho_slide

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.hope_studio.video_maker_slideshow_maker.data.VideoInSlideData

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