package com.hope_studio.video_maker_slideshow_maker.ho_models

import com.hope_studio.video_maker_slideshow_maker.ho_data.ImageWithLookupData

class ImageWithLookupDataModel(private val mImageWithLookupData: ImageWithLookupData) {

    val imagePath
    get() = mImageWithLookupData.imagePath

    val id
    get() = mImageWithLookupData.id

    var lookupType = mImageWithLookupData.lookupType
}