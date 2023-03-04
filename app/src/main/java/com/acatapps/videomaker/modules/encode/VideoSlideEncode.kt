package com.acatapps.videomaker.modules.encode

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLExt
import android.opengl.GLES20
import android.view.Surface
import com.acatapps.videomaker.data.StickerDrawerData
import com.acatapps.videomaker.data.StickerForRenderData
import com.acatapps.videomaker.ffmpeg.FFmpeg
import com.acatapps.videomaker.ffmpeg.FFmpegCmd
import com.acatapps.videomaker.image_slide_show.drawer.ImageSlideData
import com.acatapps.videomaker.image_slide_show.drawer.ImageSlideDataContainer
import com.acatapps.videomaker.image_slide_show.drawer.ImageSlideDrawer
import com.acatapps.videomaker.image_slide_show.drawer.ImageSlideThemeDrawer
import com.acatapps.videomaker.slide_show_package_2.slide_show_gl_view_2.StickerDrawer
import com.acatapps.videomaker.slide_show_theme.data.ThemeData
import com.acatapps.videomaker.slide_show_transition.transition.GSTransition
import com.acatapps.videomaker.utils.BitmapUtils
import com.acatapps.videomaker.utils.FileUtils
import com.acatapps.videomaker.utils.Logger
import com.acatapps.videomaker.utils.MediaUtils
import com.acatapps.videomaker.video_player.VideoPlayDrawer
import kotlin.math.roundToInt

class VideoSlideEncode(
    private val videoPath: String,
    private val stickerAddedList: ArrayList<StickerForRenderData>,
    private val videoQuality: Int
) {

    private var mWidth = videoQuality
    private var mHeight = videoQuality
    private var mBitRare = 2500000

    var totalTimeForGetFrame = 0L


    private val MIME_TYPE = "video/avc"
    private val FRAME_RATE = 24
    private val IFRAME_INTERVAl = 5
    private val NUMBER_FRAMES = FRAME_RATE * 60

    private var mEncoder = MediaCodec.createEncoderByType(MIME_TYPE)
    private lateinit var mInputSurface: CodecInputSurface
    private lateinit var mMuxer: MediaMuxer

    private var mTrackIndex = 0
    private var mMuxerStarted = false

    private val mBufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()

    private var outputFilePath = ""
    private val mStickerDrawerDataList = ArrayList<StickerDrawerData>()

    fun performEncodeVideo(onUpdateProgress: (Float) -> Unit, onComplete: (String) -> Unit):String {
        mBitRare = MediaUtils.getVideoBitRare(videoPath)
        try {
            val start = System.currentTimeMillis()
            prepareEncode()
            mInputSurface.makeCurrent()

            for (item in stickerAddedList) {
                val drawer = StickerDrawer().apply {
                    prepare(BitmapUtils.getStickerFromFilePath(item.stickerPath))
                }
                val stickerDrawerData = StickerDrawerData(item.startOffset, item.endOffset, drawer)
                mStickerDrawerDataList.add(stickerDrawerData)
            }
            GLES20.glClearColor(0f, 0f, 0f, 1f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            val videoDuration = MediaUtils.getVideoDuration(videoPath)
            val videoPlayDrawer = VideoPlayDrawer(videoPath)
            videoPlayDrawer.prepare(0f)

            var time = 0L
            var timeMarker = System.currentTimeMillis()
            while (true) {
                drainEncoder(false)
                val deltaTime = System.currentTimeMillis()-timeMarker
                timeMarker = System.currentTimeMillis()
                time+=deltaTime
                videoPlayDrawer.drawFrame()
                mInputSurface.setPresentationTime(time * 1000000L)
                mInputSurface.swapBuffer()
                Logger.e("time = $time")
                time+=40
                if(time >= videoDuration) break

            }
            videoPlayDrawer.onDestroy()

            drainEncoder(true)
            releaseEncoder()
            val delta = System.currentTimeMillis() - start
            Logger.e("total time = $delta  --- ${(delta / 1000f).roundToInt()}")
        } catch (e: Exception) {

        } finally {
        }
        return outputFilePath
    }

    fun releaseEncoder() {

        if (mEncoder != null) {
            mEncoder.stop()
            mEncoder.release()

        }
        if (mInputSurface != null) {
            mInputSurface.release()
        }
        if (mMuxer != null) {
            mMuxer.stop()
            mMuxer.release()

        }
    }

    private fun prepareEncode() {
        val mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight).apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
            setInteger(MediaFormat.KEY_BIT_RATE, mBitRare)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAl)
        }
        mEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mInputSurface = CodecInputSurface(mEncoder.createInputSurface())
        mEncoder.start()
        outputFilePath = FileUtils.getTempVideoPath()
        try {
            mMuxer = MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        } catch (e: java.lang.Exception) {

        }
        mTrackIndex = -1
        mMuxerStarted = false
    }

    private fun drainEncoder(enOfStream: Boolean) {
        val timeOutUSec = 10000L
        if (enOfStream) {
            mEncoder.signalEndOfInputStream()
        }

        var encoderOutputBuffers = mEncoder.outputBuffers
        while (true) {
            val encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, timeOutUSec)
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!enOfStream) {
                    break
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {

                encoderOutputBuffers = mEncoder.outputBuffers
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                if (mMuxerStarted) {
                    throw RuntimeException("format changed twice")
                }
                val mediaFormat = mEncoder.outputFormat
                mTrackIndex = mMuxer.addTrack(mediaFormat)
                mMuxer.start()
                mMuxerStarted = true
            } else if (encoderStatus < 0) {
                Logger.e("unexpected result from encoder.dequeueOutputBuffer: $encoderStatus")
            } else {

                val encodedData = encoderOutputBuffers[encoderStatus]

                if (encodedData == null) {
                    Logger.e("encoderOutputBuffer $encoderStatus was null")
                    throw RuntimeException("encoderOutputBuffer $encoderStatus was null")
                }

                if ((mBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    mBufferInfo.size = 0

                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw RuntimeException("muxer hasn't started")
                    }

                    encodedData.position(mBufferInfo.offset)
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size)

                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo)
                }
                mEncoder.releaseOutputBuffer(encoderStatus, false)
                if (mBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    if (!enOfStream) {
                        Logger.e("reached end of stream unexpectedly")
                    } else {
                        Logger.e("end of stream reached")
                    }
                    break
                }
            }
        }
    }


    private class CodecInputSurface(val mSurface: Surface) {
        private val EGL_RECORDABLE_ANDROID = 0x3142

        private var mEGLDisplay = EGL14.EGL_NO_DISPLAY
        private var mEGLContext = EGL14.EGL_NO_CONTEXT
        private var mEGLSurface = EGL14.EGL_NO_SURFACE

        init {
            eglSetup()
        }

        fun eglSetup() {
            mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
                throw RuntimeException("unable to get EGL14 display")
            }

            val version = IntArray(2)
            if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
                throw RuntimeException("unable to initialize EGL14");
            }

            val attrList = intArrayOf(
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL_RECORDABLE_ANDROID, 1,
                EGL14.EGL_NONE
            )

            val configs: Array<EGLConfig?> = arrayOfNulls(1)
            val numConfigs = IntArray(1)
            EGL14.eglChooseConfig(mEGLDisplay, attrList, 0, configs, 0, configs.size, numConfigs, 0)
            val attr_list = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
            mEGLContext =
                EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT, attr_list, 0)

            val surfaceAttrs = intArrayOf(EGL14.EGL_NONE)
            mEGLSurface =
                EGL14.eglCreateWindowSurface(mEGLDisplay, configs[0], mSurface, surfaceAttrs, 0)
        }

        fun makeCurrent() {
            EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
        }

        fun swapBuffer(): Boolean {
            return EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface)
        }

        fun setPresentationTime(nSecs: Long) {
            EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, nSecs)
        }

        fun release() {
            if (mEGLDisplay !== EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(
                    mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_CONTEXT
                )
                EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface)
                EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)
                EGL14.eglReleaseThread()
                EGL14.eglTerminate(mEGLDisplay)
            }
            mSurface.release()
            mEGLDisplay = EGL14.EGL_NO_DISPLAY
            mEGLContext = EGL14.EGL_NO_CONTEXT
            mEGLSurface = EGL14.EGL_NO_SURFACE

        }

    }

}