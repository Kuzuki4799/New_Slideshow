package com.acatapps.videomaker.data

import android.graphics.Bitmap
import com.acatapps.videomaker.utils.BitmapUtils
import java.io.Serializable

data class StickerForRenderData(val stickerPath: String, val startOffset: Int, val endOffset: Int) : Serializable
