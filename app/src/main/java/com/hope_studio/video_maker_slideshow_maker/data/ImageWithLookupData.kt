package com.hope_studio.video_maker_slideshow_maker.data

import com.hope_studio.video_maker_slideshow_maker.utils.LookupUtils

class ImageWithLookupData(val id:Int, val imagePath:String, var lookupType: LookupUtils.LookupType=LookupUtils.LookupType.NONE)