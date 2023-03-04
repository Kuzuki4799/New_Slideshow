package com.acatapps.videomaker.models

import com.acatapps.videomaker.data.LookupData

class LookupDataModel(private val mLookupData: LookupData) {
    val name
    get() = mLookupData.name

    val lookupType
    get() = mLookupData.lookupType
}