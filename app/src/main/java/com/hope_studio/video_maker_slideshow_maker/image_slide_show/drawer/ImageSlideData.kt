package com.hope_studio.video_maker_slideshow_maker.image_slide_show.drawer

import com.hope_studio.video_maker_slideshow_maker.utils.LookupUtils
import java.io.Serializable

class ImageSlideData(val slideId:Long, val fromImagePath:String, val toImagePath:String, var lookupType: LookupUtils.LookupType = LookupUtils.LookupType.NONE):Serializable