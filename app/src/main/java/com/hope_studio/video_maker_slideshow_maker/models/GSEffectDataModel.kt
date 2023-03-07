package com.hope_studio.video_maker_slideshow_maker.models

import com.hope_studio.video_maker_slideshow_maker.data.GSEffectData
import com.hope_studio.video_maker_slideshow_maker.gs_effect.GSEffectUtils

class GSEffectDataModel (val gsEffectData: GSEffectData) {
    val gsEffect = GSEffectUtils.getEffectByType(gsEffectData.effectType)
    val name = gsEffect.name
    var isSelect = false
}