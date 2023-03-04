package com.acatapps.videomaker.image_slide_show.drawer

import com.acatapps.videomaker.utils.LookupUtils
import java.io.Serializable

class ImageSlideData(val slideId:Long, val fromImagePath:String, val toImagePath:String, var lookupType: LookupUtils.LookupType = LookupUtils.LookupType.NONE):Serializable