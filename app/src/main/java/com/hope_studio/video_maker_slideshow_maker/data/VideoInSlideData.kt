package com.hope_studio.video_maker_slideshow_maker.data

import android.view.View
import com.hope_studio.video_maker_slideshow_maker.gs_effect.GSEffectUtils
import java.io.Serializable

class VideoInSlideData(val path:String, val id:Int=View.generateViewId(), var gsEffectType: GSEffectUtils.EffectType=GSEffectUtils.EffectType.NONE):Serializable {
}