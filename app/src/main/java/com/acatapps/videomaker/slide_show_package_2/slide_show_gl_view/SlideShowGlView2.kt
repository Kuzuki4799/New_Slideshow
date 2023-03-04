package com.acatapps.videomaker.slide_show_package_2.slide_show_gl_view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.acatapps.videomaker.slide_show_theme.data.ThemeData
import com.acatapps.videomaker.slide_show_transition.transition.GSTransition

class SlideShowGlView2 : GLSurfaceView {

    var mSlideShowRenderer:SlideShowRenderer? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attributes: AttributeSet) : super(context, attributes) {
        init()
    }

    private fun init() {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
    }

    fun performSetRenderer(slideShowRenderer: SlideShowRenderer) {
        mSlideShowRenderer = slideShowRenderer
        setRenderer(slideShowRenderer)
    }

    fun drawSlide(timeMilSec:Int) {
        mSlideShowRenderer?.drawSlideByTime(timeMilSec)
    }

    fun changeTransition(gsTransition: GSTransition) {
        queueEvent(Runnable {
            mSlideShowRenderer?.changeTransition(gsTransition)
        })
    }

    fun changeTheme(themeData: ThemeData) {
        queueEvent(Runnable {
           mSlideShowRenderer?.changeTheme(themeData)
        })
    }

}