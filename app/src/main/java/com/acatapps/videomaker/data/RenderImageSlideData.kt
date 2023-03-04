package com.acatapps.videomaker.data

import com.acatapps.videomaker.slide_show_theme.data.ThemeData
import com.acatapps.videomaker.slide_show_transition.transition.GSTransition
import java.io.Serializable

data class RenderImageSlideData(
    val imageList: ArrayList<String>,
    val bitmapHashMap:HashMap<String, String>,
    val videoQuality: Int,
    val delayTimeSec: Int,
    val themeData: ThemeData,
    val musicReturnData: MusicReturnData?,
    val gsTransition: GSTransition,
    val stickerAddedForRender : ArrayList<StickerForRenderData>
) :Serializable