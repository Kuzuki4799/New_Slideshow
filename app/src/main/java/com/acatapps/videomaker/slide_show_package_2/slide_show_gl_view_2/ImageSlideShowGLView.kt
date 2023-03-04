package com.acatapps.videomaker.slide_show_package_2.slide_show_gl_view_2

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.acatapps.videomaker.slide_show_theme.data.ThemeData
import com.acatapps.videomaker.slide_show_transition.transition.GSTransition

class ImageSlideShowGLView(context: Context, attributes: AttributeSet?) : GLSurfaceView(context, attributes) {

    private lateinit var mImageSlideShowRenderer:ImageSlideShowRenderer

    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
    }

    fun performSetRenderer(imageSlideShowRenderer: ImageSlideShowRenderer) {
        mImageSlideShowRenderer = imageSlideShowRenderer
        setRenderer(imageSlideShowRenderer)
    }

    fun drawSlideByTime(timeMilSec:Int) {
        mImageSlideShowRenderer.drawSlideByTime(timeMilSec)
    }

    fun changeTransition(gsTransition: GSTransition) {
        queueEvent(Runnable {
            mImageSlideShowRenderer.changeTransition(gsTransition)
        })
    }

    fun changeTheme(themeData: ThemeData) {
        queueEvent(Runnable {
            mImageSlideShowRenderer.changeTheme(themeData)
        })
    }

    fun seekTo(timeMilSec:Int) {

    }

}