package com.acatapps.videomaker.video_player_slide

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.GLES20
import android.opengl.Matrix
import android.view.Surface
import com.acatapps.videomaker.R
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.data.VideoInSlideData
import com.acatapps.videomaker.gs_effect.GSEffectUtils
import com.acatapps.videomaker.utils.Logger
import com.acatapps.videomaker.utils.MediaUtils
import com.acatapps.videomaker.utils.RawResourceReader
import com.acatapps.videomaker.utils.ShaderHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class VideoPlayerSlideDrawer(
    var videoPath: String,
    private val onCompletionListener: MediaPlayer.OnCompletionListener,
    private val onTick: (Int) -> Unit
) : SurfaceTexture.OnFrameAvailableListener {


    private var mVolume = 1f
    private val FLOAT_SIZE_BYTES = 4
    private val TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 3 * FLOAT_SIZE_BYTES
    private val TEXTURE_VERTICES_DATA_STRIDE_BYTES = 2 * FLOAT_SIZE_BYTES
    private val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0
    private val TRIANGLE_VERTICES_DATA_UV_OFFSET = 0

    private val mTriangleVerticesData = floatArrayOf(
        -1.0f, -1.0f, 0f, 1.0f,
        -1.0f, 0f, -1.0f, 1.0f, 0f, 1.0f, 1.0f, 0f
    )

    private val mTextureVerticesData = floatArrayOf(
        0f, 0.0f, 1.0f, 0f,
        0.0f, 1f, 1.0f, 1.0f
    )
    private var mTriangleVertices: FloatBuffer
    private var mTextureVertices: FloatBuffer
    private val GL_TEXTURE_EXTERNAL_OES = 0x8D65

    private val mMVPMatrix = FloatArray(16)
    private val mSTMatrix = FloatArray(16)

    private var mProgram = 0
    private var mTextureID = 0

    private var maPositionHandle = 0
    private var maTextureHandle = 0

    private var muMVPMatrixHandle = 0
    private var muSTMatrixHandle = 0

    private lateinit var mSurface: SurfaceTexture

    private var updateSurface = false
    private var mStartTimeOffset = 0
    private var mPlayer: MediaPlayer? = null

    private var mEffectCode = RawResourceReader.readTextFileFromRawResource(
        VideoMakerApplication.getContext(),
        R.raw.effect_none_code
    )

    private val mVertexShader = ("uniform mat4 uMVPMatrix;\n"
            + "uniform mat4 uSTMatrix;\n" + "attribute vec4 aPosition;\n"
            + "attribute vec4 aTextureCoord;\n"
            + "varying vec2 vTextureCoord;\n" + "void main() {\n"
            + "  gl_Position = uMVPMatrix * aPosition;\n"
            + "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" + "}\n")

    private var mFragmentShader = ("#extension GL_OES_EGL_image_external : require\n"
            + "precision highp float;\n"
            + "varying vec2 vTextureCoord;\n"
            + "uniform samplerExternalOES sTexture;\n"
            + mEffectCode
            + "void main() {\n"
            + "gl_FragColor = effect();\n"
            + "}\n")

    init {

        mTriangleVertices = ByteBuffer
            .allocateDirect(
                mTriangleVerticesData.size * FLOAT_SIZE_BYTES
            )
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTriangleVertices.put(mTriangleVerticesData).position(0)

        mTextureVertices = ByteBuffer
            .allocateDirect(
                mTextureVerticesData.size * FLOAT_SIZE_BYTES
            )
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTextureVertices.put(mTextureVerticesData).position(0)

        Matrix.setIdentityM(mSTMatrix, 0)

    }

    fun prepare(autoPlay: Boolean=true) {
        val vertexThemeShaderHandle =
            ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, mVertexShader)
        val fragmentThemeShaderHandle =
            ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShader)
        mProgram = ShaderHelper.createAndLinkProgram(
            vertexThemeShaderHandle, fragmentThemeShaderHandle,
            arrayOf("uSTMatrix", "uMVPMatrix", "aTextureCoord")
        )

        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord")
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix")

        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        mTextureID = textures[0]

        mSurface = SurfaceTexture(mTextureID)
        mSurface.setOnFrameAvailableListener(this)

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID)

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )

        synchronized(this) { updateSurface = false }

        doPlayVideo(autoPlay)
    }

    fun prepare(size: Int) {
        val vertexThemeShaderHandle =
            ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, mVertexShader)
        val fragmentThemeShaderHandle =
            ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShader)
        mProgram = ShaderHelper.createAndLinkProgram(
            vertexThemeShaderHandle, fragmentThemeShaderHandle,
            arrayOf("uSTMatrix", "uMVPMatrix", "aTextureCoord")
        )



        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord")
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix")

        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        mTextureID = textures[0]

        mSurface = SurfaceTexture(mTextureID)
        mSurface.setOnFrameAvailableListener(this)

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID)


        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )

        synchronized(this) { updateSurface = false }
        setViewPort(size)
    }

    private fun setViewPort(size: Int) {
        val viewSize = size

        val videoSize = MediaUtils.getVideoSize(videoPath)
        val viewPortX: Int
        val viewPortY: Int
        val viewPortW: Int
        val viewPortH: Int
        if (videoSize.width > videoSize.height) {
            viewPortW = viewSize
            viewPortH = viewSize * videoSize.height / videoSize.width
            viewPortY = (viewSize - viewPortH) / 2
            viewPortX = 0
        } else {
            viewPortH = viewSize
            viewPortW = viewSize * videoSize.width / videoSize.height
            viewPortY = 0
            viewPortX = (viewSize - viewPortW) / 2
        }
        GLES20.glViewport(viewPortX, viewPortY, viewPortW, viewPortH)
    }

    var mMotion = 0f
    fun drawFrame() {

        synchronized(this) {
            if (updateSurface) {
                if(mPlayer?.isPlaying == true) {
                    mSurface.updateTexImage()
                    mMotion += 1 / 25f
                    mSurface.getTransformMatrix(mSTMatrix)
                    updateSurface = false

                    onTick.invoke(mPlayer?.currentPosition ?: 0)
                }

            }
        }

        GLES20.glUseProgram(mProgram)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID)

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET)
        GLES20.glVertexAttribPointer(
            maPositionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
            mTriangleVertices
        )
        GLES20.glEnableVertexAttribArray(maPositionHandle)

        mTextureVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET)
        GLES20.glVertexAttribPointer(
            maTextureHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            TEXTURE_VERTICES_DATA_STRIDE_BYTES,
            mTextureVertices
        )
        GLES20.glEnableVertexAttribArray(maTextureHandle)

        Matrix.setIdentityM(mMVPMatrix, 0)

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0)
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0)

        val motionLocation = GLES20.glGetUniformLocation(mProgram, "_motion")
        GLES20.glUniform1f(motionLocation, mMotion)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glFinish()


    }

    fun seekTo(timeMilSec: Int) {
        mPlayer?.seekTo(timeMilSec)
        updateSurface = true
    }

    fun seekTo(timeMilSec: Int, seekCompleteListener: MediaPlayer.OnSeekCompleteListener) {
        mPlayer?.setOnSeekCompleteListener(seekCompleteListener)
        mPlayer?.seekTo(timeMilSec)
    }

    private fun doPlayVideo(autoPlay: Boolean) {
        val surface = Surface(mSurface)
        mPlayer = MediaPlayer()
        updateVolume()
        mPlayer?.setDataSource(videoPath)

        mPlayer?.setSurface(surface)
        surface.release()
        try {
            mPlayer?.setOnPreparedListener {
                mPlayer?.seekTo(mStartTimeOffset)
                mPlayer?.isLooping = false
                mPlayer?.setOnCompletionListener(onCompletionListener)
                if(autoPlay)
                playVideo()
                else pauseVideo()
            }
            mPlayer?.prepare()
        } catch (e: Exception) {

        }
    }

    fun playVideo() {

        mPlayer?.let {
            if (!it.isPlaying) {
                try {
                    it.start()
                } catch (e:Exception) {

                }


            }
        }
    }

    fun pauseVideo() {
        Logger.e("player = $mPlayer")
        mPlayer?.let {
            if (it.isPlaying) {
                mPlayer?.pause()
            }
        }
    }

    fun getCurrentPosition(): Int? = mPlayer?.currentPosition

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        synchronized(this) {
            updateSurface = true
        }
    }

    fun onDestroy() {
        mPlayer?.release()
        mPlayer = null
    }

    fun onPause() {
        Logger.e("player = $mPlayer")
        pauseVideo()
    }

    fun changeVideo(videoInSlideData: VideoInSlideData, autoPlay:Boolean=true) {
        mStartTimeOffset = 0
        GLES20.glDeleteProgram(mProgram)
        mFragmentShader = updateShader(videoInSlideData.gsEffectType)
        this.videoPath = videoInSlideData.path
        mPlayer?.release()
        mPlayer = null
        try {
            prepare()
        } catch (e:java.lang.Exception ){
            Logger.e("change video error -- ${e.message}")
        }

    }

    private var mVideoInSlideData:VideoInSlideData?=null

    fun changeVideo(videoInSlideData: VideoInSlideData, startOffset: Int,autoPlay:Boolean=true) {
        mVideoInSlideData = videoInSlideData
        mStartTimeOffset = startOffset
        GLES20.glDeleteProgram(mProgram)
        mFragmentShader = updateShader(videoInSlideData.gsEffectType)
        this.videoPath = videoInSlideData.path

        mPlayer?.release()
        mPlayer = null
        try {
            prepare(autoPlay)
            pauseVideo()
        }catch (e:java.lang.Exception) {
            Logger.e("changeVideo --> ${e.message}")
        }

    }

    fun performChangeVolume(volume: Float) {
        mVolume = volume
        updateVolume()
    }

    private fun updateVolume() {
        try {
            mPlayer?.setVolume(mVolume, mVolume)
        } catch (e:java.lang.Exception) {
            Logger.e(e.toString())
        }

    }

    private fun updateShader(effectType: GSEffectUtils.EffectType) :String{
        return ("#extension GL_OES_EGL_image_external : require\n"
                + "precision highp float;\n"
                + "varying vec2 vTextureCoord;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + "${RawResourceReader.readTextFileFromRawResource(VideoMakerApplication.getContext(), GSEffectUtils.getEffectByType(effectType).gsEffectCodeId)}"
                + "void main() {\n"
                + "gl_FragColor = effect();\n"
                + "}\n")
    }

    fun releasePlayer() {
        mPlayer?.release()
        mPlayer = null
    }
}