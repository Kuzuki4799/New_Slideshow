package com.hope_studio.base_ads.utils

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.roundToInt

object PixelUtil {

    fun dpToPx(context: Context, dp: Int) = (dp * getPixelScaleFactor(context)).roundToInt()

    fun pxToDp(context: Context, px: Int) = (px / getPixelScaleFactor(context)).roundToInt()

    private fun getPixelScaleFactor(context: Context) =
        context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT
}