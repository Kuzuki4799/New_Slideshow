package com.acatapps.videomaker.data

import java.io.Serializable

data class StickerForRenderData(val stickerPath: String, val startOffset: Int, val endOffset: Int) : Serializable
