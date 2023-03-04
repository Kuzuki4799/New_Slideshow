package com.acatapps.videomaker.video_player

import android.content.Context
import android.graphics.Canvas
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.acatapps.videomaker.utils.Logger
import com.acatapps.videomaker.utils.MediaUtils

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