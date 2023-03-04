package com.hope_studio.base_ads.utils

import android.app.Activity
import android.content.Context
import com.hope_studio.base_ads.ads.BaseAds

object TimerApp {

    private var isFistTime = false

    fun checkTimeShowConfig(context: Context, offsetTimeShowPopup: Int): Boolean {
        val timeReal = System.currentTimeMillis()
        val timeSave: Long = ShareUtils.getLong(context, BaseAds.TIME_CONFIG_SHOW, 0)
//        if (timeReal - timeSave >= offsetTimeShowPopup * 1000 || !isFistTime) {
//            setFistTime(true)
//            return true
//        }
        if (timeReal - timeSave >= offsetTimeShowPopup * 1000) {
            return true
        }
        return false
    }

    fun resetTime(activity: Activity){
        ShareUtils.putLong(activity, BaseAds.TIME_CONFIG_SHOW, 0)
    }

    fun setFistTime(isFistTime: Boolean) {
        TimerApp.isFistTime = isFistTime
    }
}