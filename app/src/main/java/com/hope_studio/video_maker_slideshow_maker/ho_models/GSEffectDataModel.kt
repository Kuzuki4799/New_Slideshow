package com.hope_studio.video_maker_slideshow_maker.ho_models

import com.hope_studio.video_maker_slideshow_maker.ho_data.GSEffectData
import com.hope_studio.video_maker_slideshow_maker.ho_gs_effect.GSEffectUtils

class GSEffectDataModel (val gsEffectData: GSEffectData) {
    val gsEffect = GSEffectUtils.getEffectByType(gsEffectData.effectType)
    val name = gsEffect.name
    var isSelect = false
}