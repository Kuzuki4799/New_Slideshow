package com.hope_studio.video_maker_slideshow_maker.ho_package_2.ho_renderer

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.hope_studio.video_maker_slideshow_maker.ho_package_2.SlideShowDrawer
import com.hope_studio.video_maker_slideshow_maker.ho_package_2.ho_data.FrameData
import com.hope_studio.video_maker_slideshow_maker.ho_theme.SlideThemeDrawer
import com.hope_studio.video_maker_slideshow_maker.ho_theme.ThemeData
import com.hope_studio.video_maker_slideshow_maker.ho_transition.hop_transition.GSTransition
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SlideRenderer(val themeData: ThemeData) : GLSurfaceView.Renderer {

    private var mSlideShowDrawer:SlideShowDrawer? = null
    private var mSlideThemeDrawer:SlideThemeDrawer? = null

    init {
        mSlideShowDrawer = SlideShowDrawer()
        mSlideThemeDrawer = SlideThemeDrawer(themeData)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mSlideThemeDrawer?.prepare()
        mSlideShowDrawer?.prepare()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

    }

    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        mSlideShowDrawer?.drawFrame()
        mSlideThemeDrawer?.drawFrame()
    }

    fun changeTheme(themeData: ThemeData) {
        mSlideThemeDrawer?.changeTheme(themeData)
        mSlideShowDrawer?.setUpdateTexture(true)
    }

    fun changeFrameData(frameData: FrameData) {
        mSlideShowDrawer?.changeFrameData(frameData)
    }

    fun changeTransition(gsTransition: GSTransition, fragmentShaderHandle: Int) {
        mSlideShowDrawer?.changeTransition(gsTransition, fragmentShaderHandle)
    }
}