package com.hope_studio.video_maker_slideshow_maker.ho_player

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.hope_studio.video_maker_slideshow_maker.ho_utils.Logger

class VideoPlayGLView(context: Context, attributes: AttributeSet?) : GLSurfaceView(context, attributes) {


    private lateinit var mVideoRenderer:VideoPlayRenderer

    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
    }

    fun performSetRenderer(videoPlayRenderer: VideoPlayRenderer) {
        mVideoRenderer = videoPlayRenderer
        setRenderer(mVideoRenderer)
        Logger.e("set renderer")
    }

}