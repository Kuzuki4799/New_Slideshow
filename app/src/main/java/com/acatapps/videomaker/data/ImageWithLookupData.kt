package com.acatapps.videomaker.data

import com.acatapps.videomaker.utils.LookupUtils

class ImageWithLookupData(val id:Int, val imagePath:String, var lookupType: LookupUtils.LookupType=LookupUtils.LookupType.NONE) {
}