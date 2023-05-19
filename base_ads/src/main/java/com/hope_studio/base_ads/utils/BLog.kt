package com.hope_studio.base_ads.utils

import android.util.Log
import com.hope_studio.base_ads.BuildConfig

object BLog {
    private const val TAG = "app-log"
    fun e(log: String) {
        if (com.hope_studio.base_ads.BuildConfig.DEBUG) Log.e(TAG, log)
    }

    fun d(log: String) {
        if (com.hope_studio.base_ads.BuildConfig.DEBUG) Log.d(TAG, log)
    }

    fun w(log: String) {
        if (com.hope_studio.base_ads.BuildConfig.DEBUG) Log.w(TAG, log)
    }
}
