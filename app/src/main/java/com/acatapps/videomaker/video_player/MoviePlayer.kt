package com.acatapps.videomaker.video_player

import android.graphics.SurfaceTexture
import android.media.*
import android.media.AudioFormat.CHANNEL_OUT_STEREO
import android.media.AudioFormat.ENCODING_PCM_16BIT
import android.view.Surface
import com.acatapps.videomaker.utils.FileUtils
import com.acatapps.videomaker.utils.Logger
import java.io.File
import java.io.FileNotFoundException


class MoviePlayer(val videoPlayDrawer2: VideoPlayDrawer2) {

    private val mSrcFilePath = "${FileUtils.internalPath}/deo60fps.mp4"
    private val mSrcFile = File(mSrcFilePath)

    private val mBufferInfo = MediaCodec.BufferInfo()

    @Volatile
    private var mIsStopRequested = false

    private var mLoop = false
    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private var mSurfaceTexture: SurfaceTexture? = null

    init {
        val extractor: MediaExtractor
        try {
            Logger.e("file path = $mSrcFilePath")
            extractor = MediaExtractor()
            extractor.setDataSource(mSrcFilePath)
            val trackIndex = selectTrackIndex(extractor)
            if (trackIndex < 0) {
                throw FileNotFoundException("no video found in $mSrcFilePath")
            }
            extractor.selectTrack(trackIndex)
            extractor.getTrackFormat(trackIndex).apply {
                mVideoWidth = getInteger(MediaFormat.KEY_WIDTH)
                mVideoHeight = getInteger(MediaFormat.KEY_HEIGHT)
            }

            mSurfaceTexture = SurfaceTexture(videoPlayDrawer2!!.textureId)

        } catch (e: Exception) {

        }
    }


    fun play() {
        val extractor: MediaExtractor
        val decoder: MediaCodec
        if (!mSrcFile.canRead()) {
            throw FileNotFoundException("can't read file $mSrcFilePath")
        }
        try {
            extractor = MediaExtractor()
            extractor.setDataSource(mSrcFilePath)
            val trackIndex = selectTrackIndex(extractor)
            if (trackIndex < 0) {
                throw FileNotFoundException("no video found in $mSrcFilePath")
            }
            extractor.selectTrack(trackIndex)

            val audioIndex = selectAudioTrackIndex(extractor)

            val format = extractor.getTrackFormat(trackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            decoder = MediaCodec.createDecoderByType(mime)

            decoder.apply {
                configure(format, null, null, 0)
                start()
            }
            doExtract(extractor, trackIndex, decoder)
        } catch (e: java.lang.Exception) {

        }
    }

    private fun doExtract(extractor: MediaExtractor, trackIndex: Int, decoder: MediaCodec) {
        val timeOutUSec = 10000L
        val decoderInputBuffers = decoder.inputBuffers
        var inputChunk = 0
        var firstInputTimeNsec = -1L
        var outputDone = false
        var inputDone = false
        while (!outputDone) {
            if (mIsStopRequested) {
                Logger.e("stop request")
                return
            }

            if (!inputDone) {
                val inputBufferIndex = decoder.dequeueInputBuffer(timeOutUSec)
                if (inputBufferIndex >= 0) {
                    if (firstInputTimeNsec == -1L) {
                        firstInputTimeNsec = System.nanoTime()
                    }
                    val inputBuf = decoderInputBuffers[inputBufferIndex]
                    val chunkSize = extractor.readSampleData(inputBuf, 0)
                    Logger.e("chunkSize = $chunkSize")
                    if (chunkSize < 0) {
                        decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        inputDone = true
                        Logger.e("end of stream")
                    } else {
                        if (extractor.sampleTrackIndex != trackIndex) {
                            Logger.e("WEIRD: got sample from track " + extractor.sampleTrackIndex + ", expected " + trackIndex)
                        }
                        val presentationTimeUs = extractor.sampleTime
                        decoder.queueInputBuffer(inputBufferIndex, 0, chunkSize, presentationTimeUs, 0)
                        inputChunk++
                        extractor.advance()
                    }
                } else {
                    Logger.e("input buffer not available")
                }
            }

            if (!outputDone) {
                val decodeStatus = decoder.dequeueOutputBuffer(mBufferInfo, timeOutUSec)
                if (decodeStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    Logger.e("not output available yet")
                } else if(decodeStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    Logger.e("decode output buffer change")
                } else if (decodeStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val newFormat = decoder.outputFormat
                    Logger.e("decoder output format changed: $newFormat")
                } else if(decodeStatus < 0) {
                    throw RuntimeException("unexpected result from decoder.dequeueOutputBuffer: $decodeStatus")
                } else {
                    if(firstInputTimeNsec != 0L) {
                        val nowSec = System.nanoTime()
                        Logger.e("startup lag ${(firstInputTimeNsec-nowSec)/1000000} ms")
                        firstInputTimeNsec = 0L
                    }
                    var doLoop = false
                    if (mBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        if(mLoop) doLoop = true
                        else outputDone = true
                    }
                    val doRender = mBufferInfo.size != 0

                    if (doRender) {

                    }


                    decoder.releaseOutputBuffer(decodeStatus, doRender)
                    if (doRender) {

                    }

                    if (doLoop) {
                        extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                        inputDone = false
                        decoder.flush() // reset decoder state

                    }
                }
            }
        }
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


    interface PlayerFeedback {
        fun playbackStopped()
    }

    interface FrameCallback {
        fun preRender(presentationTimeUsec: Long)
        fun postRender()
        fun loopReset()
    }
}