package com.hope_studio.video_maker_slideshow_maker.ho_drawer

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.hope_studio.video_maker_slideshow_maker.ho_theme.data.ThemeData
import com.hope_studio.video_maker_slideshow_maker.ho_transition.transition.GSTransition

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