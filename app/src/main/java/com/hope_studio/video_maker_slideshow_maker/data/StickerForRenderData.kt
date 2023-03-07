package com.hope_studio.video_maker_slideshow_maker.data

import java.io.Serializable

data class StickerForRenderData(val stickerPath: String, val startOffset: Int, val endOffset: Int) : Serializable
