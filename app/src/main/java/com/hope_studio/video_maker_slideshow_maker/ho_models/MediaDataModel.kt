package com.hope_studio.video_maker_slideshow_maker.ho_models

import com.hope_studio.video_maker_slideshow_maker.ho_data.MediaData

class MediaDataModel(private val mMediaData: MediaData):Comparable<MediaDataModel> {

    val filePath = mMediaData.filePath
    val dateAdded = mMediaData.dateAdded
    var count = 0
    val kind = mMediaData.mediaKind
    val duration = mMediaData.duration
    override fun compareTo(other: MediaDataModel): Int = other.dateAdded.compareTo(dateAdded)
}