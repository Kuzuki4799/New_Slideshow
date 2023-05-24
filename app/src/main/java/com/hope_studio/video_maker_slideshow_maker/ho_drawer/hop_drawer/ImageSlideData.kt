package com.hope_studio.video_maker_slideshow_maker.ho_drawer.hop_drawer

import com.hope_studio.video_maker_slideshow_maker.ho_utils.LookupUtils
import java.io.Serializable

class ImageSlideData(val slideId:Long, val fromImagePath:String, val toImagePath:String, var lookupType: LookupUtils.LookupType = LookupUtils.LookupType.NONE):Serializable