package com.acatapps.videomaker.data

import android.view.View
import com.acatapps.videomaker.gs_effect.GSEffectUtils
import java.io.Serializable

class VideoInSlideData(val path:String, val id:Int=View.generateViewId(), var gsEffectType: GSEffectUtils.EffectType=GSEffectUtils.EffectType.NONE):Serializable {
}