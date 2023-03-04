package com.acatapps.videomaker.utils

import android.annotation.SuppressLint
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.util.Size
import java.security.InvalidParameterException


object MediaUtils {

    fun getAudioDuration(path:String):Long {

        return try {
            val media = MediaMetadataRetriever()
            media.setDataSource(path)
            val duration = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?:"-1").toLong()
            duration
        } catch (e:Exception) {
            -1
        }
    }

    fun getVideoSize(videoPath:String) :Size{
        try {
            val media = MediaMetadataRetriever()
            media.setDataSource(videoPath)
            val rotation = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?:"-1").toInt()
            val videoW:Int
            val videoH:Int
            if (rotation == 90) {
                videoW = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) ?: "-1").toInt()
                videoH = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) ?: "-1").toInt()
            } else {
                videoW = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) ?: "-1").toInt()
                videoH = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) ?: "-1").toInt()
            }
            return Size(videoW, videoH)
        } catch (e:Exception) {
            return Size(1, 1)
        }


    }

    fun getVideoBitRare(videoPath:String) :Int{
        val media = MediaMetadataRetriever()
        media.setDataSource(videoPath)
        val bitRare = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
        return (bitRare ?: "-1").toInt()
    }


    fun getVideoDuration(videoPath:String) :Int{
        return try {
        val media = MediaMetadataRetriever()
        media.setDataSource(videoPath)
            val duration = (media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?:"-1").toInt()
            duration
        } catch (e:Exception) {
            0
        }
    }
    fun getVideoMimeType(videoPath:String) :String{
        return try {
            val media = MediaMetadataRetriever()
            media.setDataSource(videoPath)
            val mimeType = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE).toString()
            mimeType
        } catch (e:Exception) {
           ""
        }
    }
    fun selectVideoTrack(extractor: MediaExtractor): MediaFormat {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if ((format.getString(MediaFormat.KEY_MIME)?:"-1").startsWith("video/")) {
                extractor.selectTrack(i)
                return format
            }
        }

        throw InvalidParameterException("File contains no video track")
    }

    fun selectAudioTrack(extractor: MediaExtractor): MediaFormat {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if ((format.getString(MediaFormat.KEY_MIME) ?: "-1").startsWith("audio/")) {
                extractor.selectTrack(i)
                return format
            }
        }

        throw InvalidParameterException("File contains no audio track")
    }

    @SuppressLint("DefaultLocale")
    fun videoHasAudio(videoPath:String):Boolean {
        val media = MediaMetadataRetriever()
        media.setDataSource(videoPath)
         try {
            val hasAudio = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) ?: ""
            Logger.e("videoHasAudio = $hasAudio")
            if(hasAudio.toLowerCase() == "yes") return true
            return false

        } catch (e:Exception) {
            return false
        }
    }

}