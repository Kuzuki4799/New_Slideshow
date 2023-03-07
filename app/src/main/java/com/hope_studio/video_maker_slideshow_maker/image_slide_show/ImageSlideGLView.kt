package com.hope_studio.video_maker_slideshow_maker.image_slide_show

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.hope_studio.video_maker_slideshow_maker.slide_show_theme.data.ThemeData
import com.hope_studio.video_maker_slideshow_maker.slide_show_transition.transition.GSTransition

class ImageSlideGLView(context: Context, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {

    private lateinit var mImageSlideRenderer:ImageSlideRenderer

    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
    }

    fun doSetRenderer(imageSlideRenderer: ImageSlideRenderer) {
        mImageSlideRenderer = imageSlideRenderer

        setRenderer(imageSlideRenderer)
    }


    fun changeTransition(gsTransition: GSTransition) {

        queueEvent(Runnable {
            mImageSlideRenderer.changeTransition(gsTransition)
        })
    }




    fun changeTheme(themeData: ThemeData) {
        queueEvent(Runnable {
            mImageSlideRenderer.changeTheme(themeData)
        })
    }

}