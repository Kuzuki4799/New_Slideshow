package com.hope_studio.video_maker_slideshow_maker.data

import com.hope_studio.video_maker_slideshow_maker.ho_theme.data.ThemeData
import com.hope_studio.video_maker_slideshow_maker.ho_transition.transition.GSTransition
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