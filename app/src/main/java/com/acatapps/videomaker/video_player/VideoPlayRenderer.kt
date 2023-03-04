package com.acatapps.videomaker.video_player

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.acatapps.videomaker.utils.Logger
import com.acatapps.videomaker.utils.MediaUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class VideoPlayRenderer (val videoPath: String, val glView:GLSurfaceView, var autoPlay:Boolean = true)  : GLSurfaceView.Renderer {

    var mVideoPlayDrawer:VideoPlayDrawer? = null
    init {
       mVideoPlayDrawer = VideoPlayDrawer(videoPath, autoPlay)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        setViewPort()
        mVideoPlayDrawer?.prepare()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

    }

    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        mVideoPlayDrawer?.drawFrame()
    }

    private fun setViewPort() {
        val viewSize = glView.width

        val videoSize = MediaUtils.getVideoSize(videoPath)
        val viewPortX:Int
        val viewPortY:Int
        val viewPortW:Int
        val viewPortH:Int
        if(videoSize.width > videoSize.height) {
            viewPortW = viewSize
            viewPortH = viewSize*videoSize.height/videoSize.width
            viewPortY = (viewSize-viewPortH)/2
            viewPortX = 0
        } else {
            viewPortH = viewSize
            viewPortW = viewSize*videoSize.width/videoSize.height
            viewPortY = 0
            viewPortX = (viewSize-viewPortW)/2
        }
        GLES20.glViewport(viewPortX, viewPortY, viewPortW,viewPortH)
    }

    fun getCurrentPosition():Int? {
        return mVideoPlayDrawer?.getCurrentPosition()
    }

    fun onDestroy() {
        Logger.e("video drawer = $mVideoPlayDrawer")
        mVideoPlayDrawer?.onDestroy()
    }

    fun onPause() {
        mVideoPlayDrawer?.onPause()
    }

    fun onPlayVideo() {
        mVideoPlayDrawer?.playVideo()
    }

    fun seekTo(timeMilSec:Int) {
        mVideoPlayDrawer?.seekTo(timeMilSec)
    }

}