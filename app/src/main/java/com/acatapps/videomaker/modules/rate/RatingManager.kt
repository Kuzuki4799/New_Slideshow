package com.acatapps.videomaker.modules.rate

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.acatapps.videomaker.application.VideoMakerApplication

class RatingManager {
    private val mPreferenceName = "RatingPreference"
    private val mRatedKey = "Rated"
    private val mTimeShowRatingKey = "TimeShowRating"
    private val mVideoRenderedKey = "VideoRendered"
    private val mLowStarKey = "LowStar"
    private val mSharePreference:SharedPreferences
    private val context = VideoMakerApplication.getContext()

    companion object {
        private var instance:RatingManager? =null
        fun getInstance():RatingManager{
            if(instance == null) {
                instance = RatingManager()
            }
            return instance!!
        }
    }

    init {
        mSharePreference = context.getSharedPreferences(mPreferenceName, Context.MODE_PRIVATE)
    }


    fun setRated() {
        mSharePreference.edit().apply {
            putBoolean(mRatedKey, true)
            apply()
        }
    }

    fun isRated():Boolean = mSharePreference.getBoolean(mRatedKey, false)

    fun setTimeShowRating(timeStamp:Long) {
        mSharePreference.edit().apply {
            putLong(mTimeShowRatingKey, System.currentTimeMillis()+timeStamp)
            apply()
        }
    }

    fun getTimeShowRating():Long = mSharePreference.getLong(mTimeShowRatingKey, -1)

    fun canShowRate() :Boolean{
        if(isRated()) return false
        val timeShowRate = getTimeShowRating()
        if(timeShowRate < 0) return true
        val deltaTimeMs = System.currentTimeMillis()-timeShowRate
        return deltaTimeMs >= 0
    }



    fun getNumberVideoRendered():Int = mSharePreference.getInt(mVideoRenderedKey, 0)

    fun resetNumberVideoRendered() {
        mSharePreference.edit().apply {
            putInt(mVideoRenderedKey, 0)
            apply()
        }
    }

}