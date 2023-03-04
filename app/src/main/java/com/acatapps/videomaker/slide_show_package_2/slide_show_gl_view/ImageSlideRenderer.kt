package com.acatapps.videomaker.slide_show_package_2.slide_show_gl_view

import android.opengl.GLES20
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.slide_show_package_2.SlideShowDrawer
import com.acatapps.videomaker.slide_show_package_2.data.SlideShow
import com.acatapps.videomaker.slide_show_transition.transition.GSTransition
import com.acatapps.videomaker.utils.RawResourceReader
import com.acatapps.videomaker.utils.ShaderHelper

class ImageSlideRenderer : SlideShowRenderer() {

    private var mSlideShowDrawer: SlideShowDrawer? = null
    private lateinit var mSlideShow:SlideShow

    init {
        mSlideShowDrawer = SlideShowDrawer()
    }

    override fun initData(filePathList:ArrayList<String>) {
        mSlideShow = SlideShow(filePathList)
    }

    override fun performPrepare() {
        mSlideShowDrawer?.prepare()
    }

    override fun performDraw() {
        mSlideShowDrawer?.drawFrame()
    }

    override fun onChangeTheme() {
        mSlideShowDrawer?.setUpdateTexture(true)
    }

    override fun changeTransition(gsTransition: GSTransition) {
        val handle =  ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader(gsTransition.transitionCodeId))
        mSlideShowDrawer?.changeTransition(gsTransition, handle)
    }

    override fun drawSlideByTime(timeMilSec: Int) {
        val frameData = mSlideShow.getFrameByVideoTime(timeMilSec)
        mSlideShowDrawer?.changeFrameData(frameData)
    }

    override fun getDuration(): Int {
        return mSlideShow.getTotalDuration()
    }

    override fun getDelayTimeSec(): Int {
        return mSlideShow.delayTimeSec
    }

    override fun setDelayTimeSec(timeSec:Int):Boolean {
        if(mSlideShow.delayTimeSec == timeSec) return false
        mSlideShow.updateTime(timeSec)
        return true
    }

    override fun repeat() {
        mSlideShow.repeat()
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