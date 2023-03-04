package com.acatapps.videomaker.video_player_slide

import android.media.MediaPlayer
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.acatapps.videomaker.data.VideoInSlideData
import com.acatapps.videomaker.gs_effect.GSEffectUtils
import com.acatapps.videomaker.utils.MediaUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class VideoPlayerSlideRenderer  (var videoPath: String, private val onCompletionListener: MediaPlayer.OnCompletionListener, private val glView: GLSurfaceView, private val onTick:(Int)->Unit)  : GLSurfaceView.Renderer {

    private var mVideoPlayerSlideDrawer:VideoPlayerSlideDrawer? = null
    private var mCurrentVideoIndex = 0
    init {
        mVideoPlayerSlideDrawer = VideoPlayerSlideDrawer(videoPath, onCompletionListener,onTick)
    }



    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        setViewPort()
        mVideoPlayerSlideDrawer?.prepare()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

    }

    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        mVideoPlayerSlideDrawer?.drawFrame()
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
        return mVideoPlayerSlideDrawer?.getCurrentPosition()

    }

    fun onDestroy() {
        mVideoPlayerSlideDrawer?.onDestroy()
    }

    fun onPause() {
        mVideoPlayerSlideDrawer?.onPause()
    }

    fun onPlayVideo() {
        mVideoPlayerSlideDrawer?.playVideo()
    }

    fun seekTo(timeMilSec:Int) {
        mVideoPlayerSlideDrawer?.seekTo(timeMilSec)
    }

    fun changeVideo(videoInSlideData: VideoInSlideData) {
        this.videoPath = videoInSlideData.path
        setViewPort()
        mVideoPlayerSlideDrawer?.changeVideo(videoInSlideData)
    }

    fun changeVideo(videoInSlideData: VideoInSlideData, startOffset:Int, autoPlay:Boolean=false) {
        this.videoPath = videoInSlideData.path
        setViewPort()
        mVideoPlayerSlideDrawer?.changeVideo(videoInSlideData, startOffset,autoPlay)
    }


    fun changeVideoVolume(volume:Float) {
        mVideoPlayerSlideDrawer?.performChangeVolume(volume)
    }

    fun releasePlayer() {
        mVideoPlayerSlideDrawer?.releasePlayer()
    }

}