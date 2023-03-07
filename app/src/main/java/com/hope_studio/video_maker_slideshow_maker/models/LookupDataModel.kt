package com.hope_studio.video_maker_slideshow_maker.models

import com.hope_studio.video_maker_slideshow_maker.data.LookupData

class LookupDataModel(private val mLookupData: LookupData) {
    val name
    get() = mLookupData.name

    val lookupType
    get() = mLookupData.lookupType
}