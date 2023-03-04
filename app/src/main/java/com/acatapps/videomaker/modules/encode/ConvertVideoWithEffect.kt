package com.acatapps.videomaker.modules.encode

import android.graphics.*
import android.media.*
import android.opengl.*
import android.opengl.Matrix
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.data.StickerDrawerData
import com.acatapps.videomaker.gs_effect.GSEffect
import com.acatapps.videomaker.slide_show_package_2.slide_show_gl_view_2.StickerDrawer
import com.acatapps.videomaker.utils.*

class ConvertVideoWithEffect(val gsEffect: GSEffect= GSEffect(), val videoOutWidth:Int, val videoOutHeight:Int, val srcPath:String, val startOffset:Int,val stickerDrawerDataList:ArrayList<StickerDrawerData>,val updateProgress:(Long)->Unit) {
    companion object {
        fun textToBitmap(text: String, width: Int, height: Int): Bitmap {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            paint.textSize = 62f

            paint.color = Color.parseColor("#FF009FE3")
            val bounds = Rect()
            paint.getTextBounds(text, 0, text.length, bounds)
            paint.textSize = paint.textSize * width.toFloat() / bounds.width()



            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            paint.getTextBounds(text, 0, text.length, bounds)

            canvas.drawText(text, -bounds.left.toFloat(), -bounds.top.toFloat(), paint)
            return bitmap
        }
    }

    private val mOutMime = "video/avc"
    private var mMuxer: MediaMuxer? = null
    private var mExtractor: MediaExtractor? = null
    private var mDecoder: MediaCodec? = null
    private var mEncoder: MediaCodec? = null

    private val mMediaDecodedTimeOutUs = 10000L
    private val mBufferInfo = MediaCodec.BufferInfo()
    private var mTrackIndex = -1

    private var mAllInputExtracted = false
    private var mAllInputDecoded = false
    private var mAllOutputEncoded = false

    private var mInputSurface: Surface? = null
    private var mOutputSurface: Surface? = null

    private var mTextureRenderer:VideoEffectTextureRender? = null
    private var mSurfaceTexture: SurfaceTexture? = null


    private var eglDisplay: EGLDisplay? = null
    private var eglContext: EGLContext? = null
    private var eglSurface: EGLSurface? = null
    private val texMatrix = FloatArray(16)


    @Volatile private var frameAvailable = false

    private var thread: HandlerThread? = null

    private val lock = Object()

    private var mBitRare = 1

    private var mViewPortSize = Size(-1,-1)
    private var mViewPortOffsetX = 0
    private var mViewPortOffsetY = 0

    private var mVideoDuration = 0

    private var mTimeConvertedMs = 0L
    private var videoRenderer: StickerRender? = null
    private var mStickerRender:StickerRender? = null

    fun doConvertVideo(outPath:String){
        try {
            prepare(outPath)
            doJoinVideo()
            releaseConverter()
        } catch (e:Exception) {

        }
    }

    private fun prepare(outPath: String){

        mBitRare = MediaUtils.getVideoBitRare(srcPath)
        val videoSize = MediaUtils.getVideoSize(srcPath)

        mVideoDuration = MediaUtils.getVideoDuration(srcPath)*1000

        mStickerRender = StickerRender(false)

        val viewPortX:Int
        val viewPortY:Int
        val viewPortW:Int
        val viewPortH:Int
        if(videoSize.width > videoSize.height) {
            viewPortW = videoOutWidth
            viewPortH = videoOutWidth*videoSize.height/videoSize.width
            viewPortY = (videoOutHeight-viewPortH)/2
            viewPortX = 0
        } else {
            viewPortH = videoOutHeight
            viewPortW = videoOutHeight*videoSize.width/videoSize.height
            viewPortY = 0
            viewPortX = (videoOutWidth-viewPortW)/2
        }

        mViewPortSize = Size(viewPortW, viewPortH)
        mViewPortOffsetX = viewPortX
        mViewPortOffsetY = viewPortY

        Logger.e("view port w h = ${mViewPortSize.width} ${mViewPortSize.height} -- x y = $mViewPortOffsetX $mViewPortOffsetY -- bit rate = $mBitRare")

        mExtractor = MediaExtractor()
        mExtractor!!.setDataSource(srcPath)


        val videoTrack = selectTrackIndex(mExtractor!!)

        mExtractor!!.selectTrack(videoTrack)
        val inFormat = MediaUtils.selectVideoTrack(mExtractor!!)

        mEncoder = MediaCodec.createEncoderByType(mOutMime)

        val outFormat = getOutputFormat()

        mEncoder!!.configure(outFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mInputSurface = mEncoder!!.createInputSurface()

        initEgl()

        mTextureRenderer = VideoEffectTextureRender(
            RawResourceReader.readTextFileFromRawResource(
                VideoMakerApplication.getContext(), gsEffect.gsEffectCodeId))
        mSurfaceTexture = SurfaceTexture(mTextureRenderer!!.texId)

        thread = HandlerThread("FrameHandlerThread")
        thread!!.start()

        mSurfaceTexture!!.setOnFrameAvailableListener({
            synchronized(lock) {

                if (frameAvailable)
                    Logger.e("Frame available before the last frame was process...we dropped some frames")

                frameAvailable = true
                lock.notifyAll()
            }
        }, Handler(thread!!.looper))

        mOutputSurface = Surface(mSurfaceTexture)

        mDecoder = MediaCodec.createDecoderByType(inFormat.getString(MediaFormat.KEY_MIME) ?: "")
        mDecoder!!.configure(inFormat, mOutputSurface, null, 0)

        mMuxer = MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        mEncoder!!.start()
        mDecoder!!.start()

    }

    private fun doJoinVideo() {
        mAllInputExtracted = false
        mAllInputDecoded = false
        mAllOutputEncoded = false
        while (!mAllOutputEncoded) {

            if(!mAllInputExtracted) feedInputToDecoder()

            var encoderOutputAvailable = true
            var decoderOutputAvailable = !mAllInputDecoded

            while (encoderOutputAvailable || decoderOutputAvailable) {
                val outBufferId = mEncoder!!.dequeueOutputBuffer(mBufferInfo, mMediaDecodedTimeOutUs)
                if(outBufferId>=0) {
                    val encodedBuffer = mEncoder!!.getOutputBuffer(outBufferId)
                    mMuxer!!.writeSampleData(mTrackIndex, encodedBuffer!!, mBufferInfo)
                    mEncoder!!.releaseOutputBuffer(outBufferId, false)

                    if ((mBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        mAllOutputEncoded = true
                        break
                    }
                }else if (outBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    encoderOutputAvailable = false
                } else if (outBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    mTrackIndex = mMuxer!!.addTrack(mEncoder!!.outputFormat)
                    mMuxer!!.start()
                }
                if (outBufferId != MediaCodec.INFO_TRY_AGAIN_LATER)
                    continue
                if(!mAllInputDecoded) {
                    val bufferId = mDecoder!!.dequeueOutputBuffer(mBufferInfo, mMediaDecodedTimeOutUs)
                    if(bufferId >= 0) {
                        val render = mBufferInfo.size > 0
                        mDecoder!!.releaseOutputBuffer(bufferId, render)
                        if(render) {

                            waitTillFrameAvailable()


                            mSurfaceTexture!!.updateTexImage()
                            mSurfaceTexture!!.getTransformMatrix(texMatrix)

                            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
                            GLES20.glClearColor(0f, 0f, 0f, 0f)
                            GLES20.glViewport(0, 0, 1080, 1080)


                            EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface, mBufferInfo.presentationTimeUs * 1000)

                            EGL14.eglSwapBuffers(eglDisplay, eglSurface)

                            updateProgress.invoke(mBufferInfo.presentationTimeUs/1000)
                            mTimeConvertedMs = mBufferInfo.presentationTimeUs/1000
                        }
                        if ((mBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            mAllInputDecoded = true
                            mEncoder!!.signalEndOfInputStream()
                        }
                    }else if (outBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        decoderOutputAvailable = false
                    }
                }
            }
        }
    }

    private fun feedInputToDecoder() {
        val inBufferId = mDecoder!!.dequeueInputBuffer(mMediaDecodedTimeOutUs)
        if (inBufferId >= 0) {
            val buffer = mDecoder!!.getInputBuffer(inBufferId)
            val sampleSize = mExtractor!!.readSampleData(buffer!!, 0)

            if (sampleSize >= 0) {

                mDecoder!!.queueInputBuffer(inBufferId, 0, sampleSize, mExtractor!!.sampleTime, mExtractor!!.sampleFlags)

                mExtractor!!.advance()
            } else {
                mDecoder!!.queueInputBuffer(inBufferId, 0, 0,
                    0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                mAllInputExtracted = true
            }
        }
    }
    var EGL_RECORDABLE_ANDROID = 0x3142
    private fun initEgl() {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (eglDisplay == EGL14.EGL_NO_DISPLAY)
            throw RuntimeException("eglDisplay == EGL14.EGL_NO_DISPLAY: "
                    + GLUtils.getEGLErrorString(EGL14.eglGetError()))

        val version = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1))
            throw RuntimeException("eglInitialize(): " + GLUtils.getEGLErrorString(EGL14.eglGetError()))

        val attribList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE,
            EGL14.EGL_OPENGL_ES2_BIT,
            EGL_RECORDABLE_ANDROID, 1,
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val nConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, configs.size, nConfigs, 0))
            throw RuntimeException(GLUtils.getEGLErrorString(EGL14.eglGetError()))

        var err = EGL14.eglGetError()
        if (err != EGL14.EGL_SUCCESS)
            throw RuntimeException(GLUtils.getEGLErrorString(err))

        val ctxAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        eglContext = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttribs, 0)

        err = EGL14.eglGetError()
        if (err != EGL14.EGL_SUCCESS)
            throw RuntimeException(GLUtils.getEGLErrorString(err))

        val surfaceAttribs = intArrayOf(
            EGL14.EGL_NONE
        )
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, configs[0], mInputSurface, surfaceAttribs, 0)
        err = EGL14.eglGetError()
        if (err != EGL14.EGL_SUCCESS)
            throw RuntimeException(GLUtils.getEGLErrorString(err))

        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext))
            throw RuntimeException("eglMakeCurrent(): " + GLUtils.getEGLErrorString(EGL14.eglGetError()))
    }

    private fun waitTillFrameAvailable() {
        synchronized(lock) {
            while (!frameAvailable) {
                lock.wait(100)
                if (!frameAvailable)
                    Logger.e("Surface frame wait timed out")
            }
            frameAvailable = false
        }
    }
    private fun getOutputFormat(): MediaFormat {
        return MediaFormat.createVideoFormat(mOutMime, videoOutWidth, videoOutHeight).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, mBitRare)
            setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 0)
            setString(MediaFormat.KEY_MIME, mOutMime)
        }
    }


    private fun getMvp(): FloatArray {
        val mvp = FloatArray(16)
        Matrix.setIdentityM(mvp, 0)
        return mvp
    }
    private fun releaseConverter() {
        mExtractor!!.release()

        mDecoder?.stop()
        mDecoder?.release()
        mDecoder = null

        mEncoder?.stop()
        mEncoder?.release()
        mEncoder = null

        releaseEgl()

        mOutputSurface?.release()
        mOutputSurface = null

        mMuxer?.stop()
        mMuxer?.release()
        mMuxer = null

        thread?.quitSafely()
        thread = null

        mTrackIndex = -1
    }

    private fun releaseEgl() {
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglDestroySurface(eglDisplay, eglSurface)
            EGL14.eglDestroyContext(eglDisplay, eglContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(eglDisplay)
        }

        mInputSurface?.release()
        mInputSurface = null

        eglDisplay = EGL14.EGL_NO_DISPLAY
        eglContext = EGL14.EGL_NO_CONTEXT
        eglSurface = EGL14.EGL_NO_SURFACE
    }

    private fun selectTrackIndex(extractor: MediaExtractor): Int {
        val numberTrack = extractor.trackCount
        for (index in 0 until numberTrack) {
            val format = extractor.getTrackFormat(index)
            format.getString(MediaFormat.KEY_MIME)?.let {
                if (it.startsWith("video/")) return index
            }
        }
        return -1
    }

    private fun selectAudioTrackIndex(extractor: MediaExtractor): Int {
        val numberTrack = extractor.trackCount
        for (index in 0 until numberTrack) {
            val format = extractor.getTrackFormat(index)
            format.getString(MediaFormat.KEY_MIME)?.let {
                if (it.startsWith("audio/")) return index
            }
        }
        return -1
    }

}