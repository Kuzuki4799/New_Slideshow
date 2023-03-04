package com.acatapps.videomaker.modules.encode

import android.graphics.*
import android.media.*
import android.opengl.*
import android.opengl.Matrix
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.data.StickerForRenderData
import com.acatapps.videomaker.gs_effect.GSEffect
import com.acatapps.videomaker.utils.Logger
import com.acatapps.videomaker.utils.MediaUtils
import com.acatapps.videomaker.utils.RawResourceReader
import java.nio.ByteBuffer
import java.security.InvalidParameterException

class ConvertVideoWithEffectAndSticker(
    val gsEffect: GSEffect = GSEffect(),
    private val stickerAddedList: ArrayList<StickerForRenderData>,
    val offset: Int,
    val stickerHashMap: HashMap<String, Bitmap>,
    val onUpdateProgress: (Int) -> Unit,
    val onComplete: (Int) -> Unit
) {

    private val mOutMime = "video/avc"


    private var extractor: MediaExtractor? = null
    private var muxer: MediaMuxer? = null
    private var decoder: MediaCodec? = null
    private var encoder: MediaCodec? = null

    private val mediaCodedTimeoutUs = 10000L
    private val bufferInfo = MediaCodec.BufferInfo()
    private var trackIndex = -1
    private var audioIndex = -1

    private var allInputExtracted = false
    private var allInputDecoded = false
    private var allOutputEncoded = false


    private var inputSurface: Surface? = null
    private var outputSurface: Surface? = null


    private var videoRenderer: TextureRenderer? = null
    private var textRenderer: TextureRenderer? = null


    private var surfaceTexture: SurfaceTexture? = null


    private var eglDisplay: EGLDisplay? = null
    private var eglContext: EGLContext? = null
    private var eglSurface: EGLSurface? = null


    private val texMatrix = FloatArray(16)

    private var width = -1
    private var height = -1


    @Volatile
    private var frameAvailable = false

    private var thread: HandlerThread? = null


    private val lock = Object()


    private var mInPath = ""
    var videoQuality = 0
    private var videoOutHeight = 1080*16/9
    private var videoOutWidth = 1080
    fun process(outPath: String, inputVidFd: String, videoQuality: Int) {
        try {
            this.videoQuality = videoQuality
            videoOutWidth = videoQuality
            videoOutHeight = videoQuality*16/9
            mInPath = inputVidFd
            init(outPath, inputVidFd)
            process()
            release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {

        }
    }

    private var mHasAudio = false
    private fun init(outPath: String, inputVidFd: String) {

        extractor = MediaExtractor()
        extractor!!.setDataSource(inputVidFd)
        val inFormat = selectVideoTrack(extractor!!)


        encoder = MediaCodec.createEncoderByType(mOutMime)


        val outFormat = getOutputFormat()
        width = outFormat.getInteger(MediaFormat.KEY_WIDTH)
        height = outFormat.getInteger(MediaFormat.KEY_HEIGHT)


        encoder!!.configure(outFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        inputSurface = encoder!!.createInputSurface()


        initEgl()


        videoRenderer = TextureRenderer(
            RawResourceReader.readTextFileFromRawResource(
                VideoMakerApplication.getContext(),
                gsEffect.gsEffectCodeId
            ), true
        )
        textRenderer = TextureRenderer("", false)
        surfaceTexture = SurfaceTexture(videoRenderer!!.texId)


        thread = HandlerThread("FrameHandlerThread")
        thread!!.start()

        surfaceTexture!!.setOnFrameAvailableListener({
            synchronized(lock) {

                frameAvailable = true
                lock.notifyAll()
            }
        }, Handler(thread!!.looper))

        outputSurface = Surface(surfaceTexture)

        decoder = MediaCodec.createDecoderByType(inFormat.getString(MediaFormat.KEY_MIME) ?: "")
        decoder!!.configure(inFormat, outputSurface, null, 0)

        muxer = MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val hasAudio = MediaUtils.videoHasAudio(mInPath)
        mHasAudio = hasAudio
        Logger.e("has audio = $mHasAudio")
        if (hasAudio) {
            var audioExtractor = MediaExtractor()
            audioExtractor.setDataSource(mInPath)
            val audioFormat = MediaUtils.selectAudioTrack(audioExtractor)
            audioIndex = muxer!!.addTrack(audioFormat)
            audioExtractor.release()
        }
        encoder!!.start()
        decoder!!.start()
    }

    private fun selectVideoTrack(extractor: MediaExtractor): MediaFormat {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if ((format.getString(MediaFormat.KEY_MIME)?: "").startsWith("video/")) {
                extractor.selectTrack(i)
                return format
            }
        }

        throw InvalidParameterException("File contains no video track")
    }

    private fun getOutputFormat(): MediaFormat {
        val bitRare = when (videoQuality) {
            1080 -> {
                12000000
            }
            720 -> {
                8000000
            }
            else -> {
                3000000
            }
        }
        return MediaFormat.createVideoFormat(mOutMime, videoOutWidth, videoOutHeight).apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            setInteger(MediaFormat.KEY_BIT_RATE, bitRare)
            setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 0)
            setString(MediaFormat.KEY_MIME, mOutMime)
        }
    }

    val EGL_RECORDABLE_ANDROID = 0x3142
    private fun initEgl() {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (eglDisplay == EGL14.EGL_NO_DISPLAY)
            throw RuntimeException(
                "eglDisplay == EGL14.EGL_NO_DISPLAY: "
                        + GLUtils.getEGLErrorString(EGL14.eglGetError())
            )

        val version = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1))
            throw RuntimeException("eglInitialize(): " + GLUtils.getEGLErrorString(EGL14.eglGetError()))

        val attribList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL_RECORDABLE_ANDROID, 1,
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val nConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(
                eglDisplay,
                attribList,
                0,
                configs,
                0,
                configs.size,
                nConfigs,
                0
            )
        )
            throw RuntimeException(GLUtils.getEGLErrorString(EGL14.eglGetError()))

        var err = EGL14.eglGetError()
        if (err != EGL14.EGL_SUCCESS)
            throw RuntimeException(GLUtils.getEGLErrorString(err))

        val ctxAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        eglContext =
            EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttribs, 0)

        err = EGL14.eglGetError()
        if (err != EGL14.EGL_SUCCESS)
            throw RuntimeException(GLUtils.getEGLErrorString(err))

        val surfaceAttribs = intArrayOf(
            EGL14.EGL_NONE
        )
        eglSurface =
            EGL14.eglCreateWindowSurface(eglDisplay, configs[0], inputSurface, surfaceAttribs, 0)
        err = EGL14.eglGetError()
        if (err != EGL14.EGL_SUCCESS)
            throw RuntimeException(GLUtils.getEGLErrorString(err))

        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext))
            throw RuntimeException("eglMakeCurrent(): " + GLUtils.getEGLErrorString(EGL14.eglGetError()))
    }

    private fun process() {
        allInputExtracted = false
        allInputDecoded = false
        allOutputEncoded = false
        val viewPortX: Int
        val viewPortY: Int
        val viewPortW: Int
        val viewPortH: Int
        val videoSize = MediaUtils.getVideoSize(mInPath)
        if (videoSize.width > videoSize.height) {
            viewPortW = videoOutWidth
            viewPortH = videoOutWidth * videoSize.height / videoSize.width
            viewPortY = (videoOutHeight - viewPortH) / 2
            viewPortX = 0
        } else {
            viewPortH = videoOutHeight
            viewPortW = videoOutHeight * videoSize.width / videoSize.height
            viewPortY = 0
            viewPortX = (videoOutWidth - viewPortW) / 2
        }
        var processOffset = 0
        while (!allOutputEncoded) {
            if (!allInputExtracted)
                feedInputToDecoder()

            var encoderOutputAvailable = true
            var decoderOutputAvailable = !allInputDecoded

            while (encoderOutputAvailable || decoderOutputAvailable) {

                val outBufferId = encoder!!.dequeueOutputBuffer(bufferInfo, mediaCodedTimeoutUs)
                if (outBufferId >= 0) {

                    val encodedBuffer = encoder!!.getOutputBuffer(outBufferId)

                    muxer!!.writeSampleData(trackIndex, encodedBuffer!!, bufferInfo)

                    encoder!!.releaseOutputBuffer(outBufferId, false)


                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        allOutputEncoded = true
                        break
                    }
                } else if (outBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    encoderOutputAvailable = false
                } else if (outBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    trackIndex = muxer!!.addTrack(encoder!!.outputFormat)
                    muxer!!.start()
                }

                if (outBufferId != MediaCodec.INFO_TRY_AGAIN_LATER)
                    continue

                if (!allInputDecoded) {
                    val outBufferId = decoder!!.dequeueOutputBuffer(bufferInfo, mediaCodedTimeoutUs)
                    if (outBufferId >= 0) {
                        val render = bufferInfo.size > 0

                        decoder!!.releaseOutputBuffer(outBufferId, render)
                        if (render) {

                            waitTillFrameAvailable()

                            surfaceTexture!!.updateTexImage()
                            surfaceTexture!!.getTransformMatrix(texMatrix)

                            GLES20.glClearColor(0f, 0f, 0f, 1f)
                            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

                            GLES20.glEnable(GLES20.GL_BLEND)
                            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
                            GLES20.glViewport(viewPortX, viewPortY, viewPortW, viewPortH)

                            videoRenderer?.draw(getMVP(), texMatrix!!, null)
                            val time = offset + bufferInfo.presentationTimeUs / 1000
                            for (item in stickerAddedList) {
                                if (item.startOffset <= time && item.endOffset >= time) {
                                    GLES20.glViewport(viewPortX, viewPortY, viewPortW, viewPortH)
                                    textRenderer!!.draw(
                                        getBitmapMVP(),
                                        null,
                                        stickerHashMap[item.stickerPath]
                                    )
                                }
                            }
                            if (mHasAudio)
                                onUpdateProgress.invoke((bufferInfo.presentationTimeUs / 2000).toInt())
                            else
                                onUpdateProgress.invoke((bufferInfo.presentationTimeUs / 1000).toInt())
                            EGLExt.eglPresentationTimeANDROID(
                                eglDisplay, eglSurface,
                                bufferInfo.presentationTimeUs * 1000
                            )

                            EGL14.eglSwapBuffers(eglDisplay, eglSurface)
                        }


                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            allInputDecoded = true
                            encoder!!.signalEndOfInputStream()
                        }
                    } else if (outBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        decoderOutputAvailable = false
                    }
                }
            }
        }
        if(mHasAudio) {
            processOffset += MediaUtils.getVideoDuration(mInPath)/2
        } else processOffset += MediaUtils.getVideoDuration(mInPath)
        if (mHasAudio) {
            var audioExtractor = MediaExtractor()
            audioExtractor.setDataSource(mInPath)
            MediaUtils.selectAudioTrack(audioExtractor)
            val buffer = ByteBuffer.allocate(1024 * 1024)
            while (true) {
                val chunkSize = audioExtractor.readSampleData(buffer, 0)
                if (chunkSize > 0) {
                    bufferInfo.presentationTimeUs = audioExtractor.sampleTime
                    bufferInfo.flags = audioExtractor.sampleFlags
                    bufferInfo.size = chunkSize
                    muxer!!.writeSampleData(audioIndex, buffer, bufferInfo)
                    onUpdateProgress.invoke(processOffset+(bufferInfo.presentationTimeUs / 2000).toInt())
                    audioExtractor.advance()
                } else {
                    break
                }
            }
            audioExtractor.release()
        }

        onComplete.invoke(MediaUtils.getVideoDuration(mInPath))

    }

    private fun feedInputToDecoder() {
        val inBufferId = decoder!!.dequeueInputBuffer(mediaCodedTimeoutUs)
        if (inBufferId >= 0) {
            val buffer = decoder!!.getInputBuffer(inBufferId)
            val sampleSize = extractor!!.readSampleData(buffer!!, 0)

            if (sampleSize >= 0) {

                decoder!!.queueInputBuffer(
                    inBufferId, 0, sampleSize,
                    extractor!!.sampleTime, extractor!!.sampleFlags
                )

                extractor!!.advance()
            } else {
                decoder!!.queueInputBuffer(
                    inBufferId, 0, 0,
                    0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
                allInputExtracted = true
            }
        }
    }

    private fun waitTillFrameAvailable() {
        synchronized(lock) {
            while (!frameAvailable) {
                lock.wait(200)

            }
            frameAvailable = false
        }
    }

    fun release() {
        extractor!!.release()

        decoder?.stop()
        decoder?.release()
        decoder = null

        encoder?.stop()
        encoder?.release()
        encoder = null

        releaseEgl()

        outputSurface?.release()
        outputSurface = null

        muxer?.stop()
        muxer?.release()
        muxer = null

        thread?.quitSafely()
        thread = null

        width = -1
        height = -1
        trackIndex = -1
    }

    private fun releaseEgl() {
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglDestroySurface(eglDisplay, eglSurface)
            EGL14.eglDestroyContext(eglDisplay, eglContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(eglDisplay)
        }

        inputSurface?.release()
        inputSurface = null

        eglDisplay = EGL14.EGL_NO_DISPLAY
        eglContext = EGL14.EGL_NO_CONTEXT
        eglSurface = EGL14.EGL_NO_SURFACE
    }

    private fun getMVP(): FloatArray {
        val mvp = FloatArray(16)
        Matrix.setIdentityM(mvp, 0)



        return mvp
    }

    private fun getBitmapMVP(): FloatArray {
        val mvp = FloatArray(16)
        Matrix.setIdentityM(mvp, 0)


        Matrix.scaleM(mvp, 0, 1f, -1f, 1f)


        return mvp
    }


}