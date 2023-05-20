package com.hope_studio.video_maker_slideshow_maker.ho_models

import com.hope_studio.video_maker_slideshow_maker.ho_data.RecordedData

class RecordedDataModel(private val mRecordedData: RecordedData) {
    val path = mRecordedData.recordFilePath
    var isSelect = false
    val startOffset = mRecordedData.startMs
    val endOffset = mRecordedData.endMs
    fun checkTime(timeMs:Int):Boolean {
        if(timeMs >= mRecordedData.startMs && timeMs <= mRecordedData.endMs) return true
        return false
    }
}