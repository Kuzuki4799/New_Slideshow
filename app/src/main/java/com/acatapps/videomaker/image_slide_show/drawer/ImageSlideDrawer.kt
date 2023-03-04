package com.acatapps.videomaker.image_slide_show.drawer

import android.opengl.GLES20
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.slide_show_transition.transition.GSTransition
import com.acatapps.videomaker.utils.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class ImageSlideDrawer {

    @Volatile
    private var mFrameData: ImageSlideFrame? = null

    private val mBytesPerFloat = 4


    private var mUpdateTexture = true

    private val mVertexData = floatArrayOf(
        -1f, -1f, 0.0f,
        -1f, 1f, 0.0f,
        1f, -1f, 0.0f,
        -1f, 1f, 0.0f,
        1f, 1f, 0.0f,
        1f, -1f, 0.0f
    )
    private var mVertexBuffer: FloatBuffer

    private val mPositionDataSize = 3
    private val mColorDataSize = 4
    private val mNormalDataSize = 3
    private val mTextureCoordinateDataSize = 2


    private var mProgramHandle = 0

    private var mTextureFromDataHandle = 0
    private var mTextureToDataHandle = 0

    private var mLookupTextureFromDataHandle = 0
    private var mLookupTextureToDataHandle = 0
    private var mTransition = GSTransition()

    private var mFragmentShaderHandle = 0
    private var mVertexShaderHandle = 0

    init {
        mVertexBuffer = ByteBuffer.allocateDirect(mVertexData.size * mBytesPerFloat)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mVertexBuffer.put(mVertexData).position(0)
    }

    fun prepare() {
        mVertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, getVertexShader())
        mFragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader(mTransition.transitionCodeId))

        mProgramHandle = ShaderHelper.createAndLinkProgram(mVertexShaderHandle, mFragmentShaderHandle, arrayOf("_p"))
        synchronized(this) { mUpdateTexture = true }
    }

    fun prepare(gsTransition: GSTransition) {
        mTransition = gsTransition
        mVertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, getVertexShader())
        mFragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader(mTransition.transitionCodeId))

        mProgramHandle = ShaderHelper.createAndLinkProgram(mVertexShaderHandle, mFragmentShaderHandle, arrayOf("_p"))
        synchronized(this) { mUpdateTexture = true }
    }

    fun drawFrame() {
        mFrameData?.let {
            if (mUpdateTexture) {
                    GLES20.glDeleteTextures(4, intArrayOf(mTextureFromDataHandle, mTextureToDataHandle,mLookupTextureFromDataHandle,mLookupTextureToDataHandle), 0)
                    mTextureFromDataHandle = TextureHelper.loadTexture(it.fromBitmap)
                    mTextureToDataHandle = TextureHelper.loadTexture(it.toBitmap)
                    mLookupTextureFromDataHandle = TextureHelper.loadTexture(it.fromLookupBitmap)
                    mLookupTextureToDataHandle = TextureHelper.loadTexture(it.toLookupBitmap)
                synchronized(this) { mUpdateTexture = false }
                drawSlide(it)
            } else {
                drawSlide(it)
            }

        }
    }

    private fun drawSlide(frameData: ImageSlideFrame) {
        GLES20.glClearColor(0f,0f,0f,1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(mProgramHandle)

        val positionAttr = GLES20.glGetAttribLocation(mProgramHandle, "_p")
        GLES20.glEnableVertexAttribArray(positionAttr)
        GLES20.glVertexAttribPointer(positionAttr, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer)

        val textureFromLocate = GLES20.glGetUniformLocation(mProgramHandle, "from")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureFromDataHandle)
        GLES20.glUniform1i(textureFromLocate, 0)

        val textureToLocate = GLES20.glGetUniformLocation(mProgramHandle, "to")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureToDataHandle)
        GLES20.glUniform1i(textureToLocate, 1)

        val lookupTextureFromLocate = GLES20.glGetUniformLocation(mProgramHandle, "fromLookupTexture")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLookupTextureFromDataHandle)
        GLES20.glUniform1i(lookupTextureFromLocate, 2)

        val lookupTextureToLocate = GLES20.glGetUniformLocation(mProgramHandle, "toLookupTexture")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLookupTextureToDataHandle)
        GLES20.glUniform1i(lookupTextureToLocate, 3)

        val zoomProgressLocation = GLES20.glGetUniformLocation(mProgramHandle, "_zoomProgress")
        GLES20.glUniform1f(zoomProgressLocation, frameData.zoomProgress)

        val zoom1ProgressLocation = GLES20.glGetUniformLocation(mProgramHandle, "_zoomProgress1")
        GLES20.glUniform1f(zoom1ProgressLocation, frameData.zoomProgress1)

        val progressLocation = GLES20.glGetUniformLocation(mProgramHandle, "progress")
        GLES20.glUniform1f(progressLocation, frameData.progress)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
    }


    fun setUpdateTexture(b: Boolean) {
        mUpdateTexture = b
        Logger.e("setUpdateTexture = $b")
        if(b) {
            mFrameData = null
            synchronized(this) {
                mUpdateTexture = true
            }
        }
    }

    fun reset() {
        mFrameData = null
        synchronized(this) {
            mUpdateTexture = true
        }
    }

    fun changeFrameData(frameData: ImageSlideFrame?, isRender:Boolean = false) {


        if(!isRender) {
            mFrameData = frameData
            return
        }



        if(mFrameData == null) {
            mFrameData = frameData
            synchronized(this) {
                mUpdateTexture = true
            }

        } else {
            mFrameData?.zoomProgress = frameData?.zoomProgress ?: 1f
            mFrameData?.zoomProgress1 = frameData?.zoomProgress1 ?: 1f
            if (mFrameData?.slideId == frameData?.slideId ) {
                mFrameData?.progress = frameData?.progress ?: 0f

                synchronized(this) {
                    mUpdateTexture = false
                }
            } else {
                mFrameData = frameData
                synchronized(this) {
                    mUpdateTexture = true
                }

            }
        }


    }

    fun changeTransition(gsTransition: GSTransition, fragmentShaderHandle: Int) {
        if (mTransition.transitionCodeId == gsTransition.transitionCodeId) return
        mTransition = gsTransition
        mFragmentShaderHandle = fragmentShaderHandle
        GLES20.glDeleteProgram(mProgramHandle)
        mProgramHandle = ShaderHelper.createAndLinkProgram(
            mVertexShaderHandle, mFragmentShaderHandle,
            arrayOf("_p")
        )
    }

    private fun getVertexShader(): String {
        return "attribute vec2 _p;\n" +
                "varying vec2 _uv;\n" +
                "void main() {\n" +
                "gl_Position = vec4(_p,0.0,1.0);\n" +
                "_uv = vec2(0.5, 0.5) * (_p+vec2(1.0, 1.0));\n" +
                "}"
    }



    private fun getFragmentShader(transitionCodeId: Int): String {

        val transitionCode = RawResourceReader.readTextFileFromRawResource(
            VideoMakerApplication.getContext(),
            transitionCodeId
        )

        return "precision highp float;" +
                "varying highp vec2 _uv;\n" +
                "uniform sampler2D from, to;\n" +
                "uniform sampler2D fromLookupTexture, toLookupTexture;\n" +
                "uniform float progress, ratio, _fromR, _toR;\n" +
                "uniform highp float _zoomProgress,_zoomProgress1;\n" +
                "\n" +
                "vec4 lookup(vec4 textureColor, sampler2D lookupBitmap, vec2 uv) {\n" +
                "    //highp vec4 textureColor = texture2D(inputTexture, uv);\n" +
                "\n" +
                "    highp float blueColor = textureColor.b * 63.0;\n" +
                "\n" +
                "    highp vec2 quad1;\n" +
                "    quad1.y = floor(floor(blueColor) / 8.0);\n" +
                "    quad1.x = floor(blueColor) - (quad1.y * 8.0);\n" +
                "\n" +
                "    highp vec2 quad2;\n" +
                "    quad2.y = floor(ceil(blueColor) / 8.0);\n" +
                "    quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n" +
                "\n" +
                "    highp vec2 texPos1;\n" +
                "    texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
                "    texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
                "\n" +
                "    highp vec2 texPos2;\n" +
                "    texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
                "    texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
                "\n" +
                "    lowp vec4 newColor1 = texture2D(lookupBitmap, texPos1);\n" +
                "    lowp vec4 newColor2 = texture2D(lookupBitmap, texPos2);\n" +
                "\n" +
                "    lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n" +
                "\n" +
                "    return mix(textureColor, vec4(newColor.rgb, textureColor.w), 1.);\n" +
                "}\n" +
                "\n" +
                "vec4 getFromColor(vec2 uv){\n" +
                ""+
                "    return lookup(texture2D(from, vec2(1.0, -1.0)*uv*_zoomProgress), fromLookupTexture, _uv);\n" +
                "}\n" +
                "vec4 getToColor(vec2 uv){\n" +
                "    return lookup(texture2D(to, vec2(1.0, -1.0)*uv*_zoomProgress1), toLookupTexture, _uv) ;\n" +
                "}\n" +
                "\n" +
                transitionCode+
                "void main()\n" +
                "{\n" +
                "    gl_FragColor=transition(_uv);\n" +
                "}"
    }
}