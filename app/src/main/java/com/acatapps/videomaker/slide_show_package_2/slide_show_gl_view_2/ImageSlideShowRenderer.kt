package com.acatapps.videomaker.slide_show_package_2.slide_show_gl_view_2

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.slide_show_package_2.SlideShowDrawer
import com.acatapps.videomaker.slide_show_package_2.data.SlideShow
import com.acatapps.videomaker.slide_show_theme.SlideThemeDrawer
import com.acatapps.videomaker.slide_show_theme.data.ThemeData
import com.acatapps.videomaker.slide_show_transition.transition.GSTransition
import com.acatapps.videomaker.utils.RawResourceReader
import com.acatapps.videomaker.utils.ShaderHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ImageSlideShowRenderer() : GLSurfaceView.Renderer {

    private var mSlideShowDrawer:SlideShowDrawer? = null

    private var mThemeDrawer:SlideThemeDrawer? = null

    private var mThemeData = ThemeData()
    val themeData get() = mThemeData

    private lateinit var mSlideShow:SlideShow
    val slideShow get() = mSlideShow

    private var mGSTransition = GSTransition()
    val gsTransition get() = mGSTransition

    init {
        mThemeDrawer = SlideThemeDrawer(mThemeData)
        mSlideShowDrawer = SlideShowDrawer()
    }

    fun initData(imageList: ArrayList<String>) {
        mSlideShow = SlideShow(imageList)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mSlideShowDrawer?.prepare()
        mThemeDrawer?.prepare()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

    }

    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        mSlideShowDrawer?.drawFrame()
        mThemeDrawer?.drawFrame()
    }

    fun drawSlideByTime(timeMilSec:Int) {
        val frameData = mSlideShow.getFrameByVideoTime(timeMilSec)
        mSlideShowDrawer?.changeFrameData(frameData)
    }

     fun changeTheme(themeData:ThemeData) {
         mThemeData = themeData
         mThemeDrawer?.changeTheme(themeData)
         mSlideShowDrawer?.setUpdateTexture(true)
    }

     fun changeTransition(gsTransition: GSTransition) {
         mGSTransition = gsTransition
        val handle =  ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader(gsTransition.transitionCodeId))
        mSlideShowDrawer?.changeTransition(gsTransition, handle)
    }

    fun getMaxDuration():Int = mSlideShow.getTotalDuration()

    fun getDelayTimeSec():Int = mSlideShow.delayTimeSec

    fun changeDelayTimeSec(delayTime:Int) {
        mSlideShow.updateTime(delayTime)
    }

    fun repeat() {
        mSlideShow.repeat()
    }

    fun onPlay() {
        mThemeDrawer?.playTheme()
    }

    fun onPause() {
        mThemeDrawer?.pauseTheme()
    }

    fun seekTo(timeMilSec:Int, onComplete:()->Unit) {
        mSlideShow.seekTo(timeMilSec, onComplete)
    }

    private fun getFragmentShader(transitionCodeId: Int): String {
        val transitionCode = RawResourceReader.readTextFileFromRawResource(VideoMakerApplication.getContext(), transitionCodeId)
        return "precision mediump float;\n" +
                "varying vec2 _uv;\n" +
                "uniform sampler2D from, to;\n" +
                "uniform float progress, ratio, _fromR, _toR, _zoomProgress;\n" +
                "\n" +
                "vec4 getFromColor(vec2 uv){\n" +
                "    return texture2D(from, vec2(1.0, -1.0)*uv*_zoomProgress);\n" +
                "}\n" +
                "vec4 getToColor(vec2 uv){\n" +
                "    return texture2D(to, vec2(1.0, -1.0)*uv*_zoomProgress);\n" +
                "}"+
                transitionCode +
                "void main(){gl_FragColor=transition(_uv);}"
    }

}