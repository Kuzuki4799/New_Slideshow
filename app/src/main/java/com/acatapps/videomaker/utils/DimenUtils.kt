package com.acatapps.videomaker.utils

import android.content.Context
import com.acatapps.videomaker.application.VideoMakerApplication

object DimenUtils {
    fun screenWidth(context: Context) : Int = context.resources.displayMetrics.widthPixels

    fun screenHeight(context: Context) : Int = context.resources.displayMetrics.heightPixels

    fun density(context: Context) : Float = context.resources.displayMetrics.density

    fun density() : Float = VideoMakerApplication.getContext().resources.displayMetrics.density

    fun videoPreviewScale():Float {
        val context = VideoMakerApplication.getContext()
        val screenH = screenHeight(context)
        val screenW = screenWidth(context)
        val toolAreaHeightMin = 356*density(context)

        return if((screenH-toolAreaHeightMin) < screenW) {
            (screenH-toolAreaHeightMin)/screenW
        } else {
            1f
        }
    }


    fun videoScaleInTrim():Float {
        val context = VideoMakerApplication.getContext()
        val screenH = screenHeight(context)
        val screenW = screenWidth(context)
        val toolAreaHeightMin = 236*density(context)

        return if((screenH-toolAreaHeightMin) < screenW) {
            (screenH-toolAreaHeightMin)/screenW
        } else {
            1f
        }
    }

}