package com.acatapps.videomaker.models

import com.acatapps.videomaker.data.ImageWithLookupData

class ImageWithLookupDataModel(private val mImageWithLookupData: ImageWithLookupData) {

    val imagePath
    get() = mImageWithLookupData.imagePath

    val id
    get() = mImageWithLookupData.id

    var lookupType = mImageWithLookupData.lookupType
}