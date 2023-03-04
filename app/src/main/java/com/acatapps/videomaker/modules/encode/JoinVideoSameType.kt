package com.acatapps.videomaker.modules.encode

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaMuxer
import android.widget.Toast
import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.utils.FileUtils
import com.acatapps.videomaker.utils.Logger
import com.acatapps.videomaker.utils.MediaUtils
import java.nio.ByteBuffer

class JoinVideoSameType(val videoPathList:ArrayList<String>, val videoConvertedHashMap:HashMap<String,String>) {
    private val maxChunkSize = 1024 * 1024

    private var mOutPath = FileUtils.getOutputVideoPath()
    private var mVideoIndex = -1
    private var mAudioIndex = -1
    fun doJoin(updateProgress:(Long)->Unit):String {
        val tempVideoPath = FileUtils.getTempVideoPath()
        val muxer = MediaMuxer(tempVideoPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        for(videoPath in videoPathList) {

            if(MediaUtils.videoHasAudio(videoPath)) {
                var videoExtractor = MediaExtractor()
                videoExtractor.setDataSource(videoConvertedHashMap[videoPath] ?: videoPath)

                var audioExtractor = MediaExtractor()
                audioExtractor.setDataSource(videoPath)

                val audioFormat = MediaUtils.selectAudioTrack(audioExtractor)
                val videoFormat = MediaUtils.selectVideoTrack(videoExtractor)

                mAudioIndex = muxer.addTrack(audioFormat)
                mVideoIndex = muxer.addTrack(videoFormat)

                break
            }
        }



        if(mVideoIndex == -1) {
            var extractor = MediaExtractor()
            extractor.setDataSource(videoPathList[0])
            val videoFormat = MediaUtils.selectVideoTrack(extractor)
            mVideoIndex = muxer.addTrack(videoFormat)

        }

        muxer.start()
        val buffer = ByteBuffer.allocate(maxChunkSize)
        val bufferInfo = MediaCodec.BufferInfo()
        var videoTimeOffset = 0L
        var audioTimeOffset = 0L

        for(videoPath in videoPathList) {
            var timeConverted = 0L
            val hasAudio = MediaUtils.videoHasAudio(videoPath)
            Logger.e("has audio $hasAudio -- $videoPath")
            if(hasAudio) { // has audio
                val convertedPath = videoConvertedHashMap[videoPath] ?: videoPath
                Logger.e("convertedPath = $convertedPath")
                val videoExtractor = MediaExtractor()
                videoExtractor.setDataSource(convertedPath)

                val audioExtractor = MediaExtractor()
                audioExtractor.setDataSource(videoPath)

                MediaUtils.selectAudioTrack(audioExtractor)
                MediaUtils.selectVideoTrack(videoExtractor)

                while (true) {
                    val chunkSize = videoExtractor.readSampleData(buffer, 0)
                    if(chunkSize >= 0) {

                        bufferInfo.presentationTimeUs = videoExtractor.sampleTime+videoTimeOffset
                        bufferInfo.flags = videoExtractor.sampleFlags
                        bufferInfo.size = chunkSize

                        muxer.writeSampleData(mVideoIndex, buffer, bufferInfo)
                        updateProgress.invoke(bufferInfo.presentationTimeUs/2000 - timeConverted)
                        timeConverted = bufferInfo.presentationTimeUs/2000
                        videoExtractor.advance()

                    } else {

                        break
                    }
                }
                timeConverted=0L
                while (true) {
                    val chunkSize = audioExtractor.readSampleData(buffer, 0)
                    if (chunkSize > 0) {
                        bufferInfo.presentationTimeUs = audioExtractor.sampleTime+audioTimeOffset
                        bufferInfo.flags = audioExtractor.sampleFlags
                        bufferInfo.size = chunkSize
                        muxer.writeSampleData(mAudioIndex, buffer, bufferInfo)
                        updateProgress.invoke(bufferInfo.presentationTimeUs/2000 - timeConverted)
                        timeConverted = bufferInfo.presentationTimeUs/2000
                        audioExtractor.advance()
                    } else {
                        break
                    }
                }

            } else {
                 timeConverted = 0L
                val videoExtractor = MediaExtractor()
                videoExtractor.setDataSource(videoConvertedHashMap[videoPath] ?: videoPath)
                MediaUtils.selectVideoTrack(videoExtractor)

                while (true) {
                    val chunkSize = videoExtractor.readSampleData(buffer, 0)
                    if(chunkSize >= 0) {

                        bufferInfo.presentationTimeUs = videoExtractor.sampleTime+videoTimeOffset
                        bufferInfo.flags = videoExtractor.sampleFlags
                        bufferInfo.size = chunkSize

                        muxer.writeSampleData(mVideoIndex, buffer, bufferInfo)
                        updateProgress.invoke(bufferInfo.presentationTimeUs/1000 - timeConverted)
                        timeConverted = bufferInfo.presentationTimeUs/1000
                        videoExtractor.advance()

                    } else {
                        break
                    }
                }
            }
            val time = MediaUtils.getVideoDuration(videoPath)*1000
            videoTimeOffset+=time
            audioTimeOffset+=time
        }
        muxer.stop()
        muxer.release()
        FileUtils.copyFileTo(tempVideoPath, mOutPath)
        return mOutPath
    }

}