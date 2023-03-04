package com.acatapps.videomaker.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Paint
import android.graphics.Rect
import android.net.ConnectivityManager
import android.os.Environment
import android.os.StatFs
import android.os.StrictMode
import android.view.View
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.acatapps.videomaker.R
import com.acatapps.videomaker.application.VideoMakerApplication
import kotlinx.android.synthetic.main.item_native_ads_in_my_studio.view.*
import kotlinx.android.synthetic.main.native_ad_big.view.*
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

object Utils {

    fun convertSecToTimeString(sec: Int): String {
        return if (sec >= 3600) {
            val h = zeroPrefix((sec / 3600).toString())
            val m = zeroPrefix(((sec % 3600) / 60).toString())
            val s = zeroPrefix(((sec % 3600) % 60).toString())
            "$h:$m:$s"
        } else {
            val m = zeroPrefix(((sec % 3600) / 60).toString())
            val s = zeroPrefix(((sec % 3600) % 60).toString())
            "$m:$s"
        }
    }

    private fun zeroPrefix(string: String):String {
        if(string.length<2) return "0$string"
        return string
    }
     fun getTextWidth(text:String, paint: Paint):Float {
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.width().toFloat()
    }
     fun getTextHeight(text:String, paint: Paint):Float {
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.height().toFloat()
    }
    fun getAvailableSpaceInMB(): Long {
        val SIZE_KB = 1024L
        val SIZE_MB = SIZE_KB * SIZE_KB
        val availableSpace: Long
        try {
            val stat = StatFs(getVideoAppDirectory())
            availableSpace = stat.availableBlocksLong * stat.blockSizeLong
        } catch (e: java.lang.Exception) {
            return 120
        }
        return availableSpace / SIZE_MB
    }
    fun getVideoAppDirectory(): String? {
        val folder =
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                VideoMakerApplication.getContext().getString(R.string.app_name)
            )
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder.absolutePath
    }

    fun bindBigNativeAds(nativeAd: UnifiedNativeAd, adView: UnifiedNativeAdView?) {
        if(adView == null) return

        adView.headlineView = adView.ad_headline
        adView.mediaView = adView.ad_media
        adView.bodyView = adView.ad_body
        adView.callToActionView = adView.ad_call_to_action
        adView.iconView = adView.ad_app_icon
        adView.priceView = adView.ad_price
        adView.starRatingView = adView.ad_stars
        adView.storeView = adView.ad_store
        adView.advertiserView = adView.ad_advertiser
        adView.ad_headline.text = nativeAd.headline

        if(nativeAd.body != null) {
            adView.ad_body.visibility = View.VISIBLE
            adView.ad_body.text = nativeAd.body
        } else {
            adView.ad_body.visibility = View.INVISIBLE
        }

        if(nativeAd.icon != null) {
            adView.ad_app_icon.visibility = View.VISIBLE
            adView.ad_app_icon.setImageDrawable(nativeAd.icon.drawable)
        } else {
            adView.ad_app_icon.visibility = View.INVISIBLE
        }

        if(nativeAd.callToAction != null) {
            adView.ad_call_to_action.visibility = View.VISIBLE
            adView.ad_call_to_action.text = nativeAd.callToAction

        } else {
            adView.ad_call_to_action.visibility = View.INVISIBLE
        }

        if(nativeAd.price != null) {
            adView.ad_price.visibility = View.VISIBLE
            adView.ad_price.text = nativeAd.price
        } else {
            adView.ad_price.visibility = View.INVISIBLE
        }

        if(nativeAd.store != null) {
            adView.ad_store.visibility = View.VISIBLE
            adView.ad_store.text = nativeAd.store
        } else {
            adView.ad_store.visibility = View.INVISIBLE
        }

        if(nativeAd.starRating != null) {
            adView.ad_stars.visibility = View.VISIBLE
            adView.ad_stars.rating = nativeAd.starRating.toFloat()
        } else {
            adView.ad_stars.visibility = View.INVISIBLE
        }

        if(nativeAd.advertiser != null) {
            adView.ad_advertiser.text = nativeAd.advertiser
            adView.visibility = View.VISIBLE
        } else {
            adView.visibility = View.INVISIBLE
        }

        adView.setNativeAd(nativeAd)

    }



    fun binSmallNativeAds(nativeAd: UnifiedNativeAd, adView: UnifiedNativeAdView?) {
        if(adView == null) return
        adView.iconView = adView.ad_app_icon_small
        adView.bodyView = adView.ad_body_small
        adView.callToActionView = adView.ad_call_to_action_small
        adView.headlineView = adView.ad_headline_small
        adView.advertiserView = adView.ad_attribution_small

        if(nativeAd.icon != null) {
            adView.ad_app_icon_small.setImageDrawable(nativeAd.icon.drawable)
            adView.ad_app_icon_small.visibility = View.VISIBLE
        } else {
            adView.ad_app_icon_small.visibility = View.GONE
        }

        if(nativeAd.callToAction != null) {
            adView.ad_call_to_action_small.text = nativeAd.callToAction
            adView.ad_call_to_action_small.visibility = View.VISIBLE
        } else {
            adView.ad_call_to_action_small.visibility = View.GONE
        }

        if(nativeAd.body != null) {
            adView.ad_body_small.text = nativeAd.body
            adView.ad_body_small.visibility = View.VISIBLE
        } else {
            adView.ad_body_small.visibility = View.GONE
        }

        if(nativeAd.headline != null) {
            adView.ad_headline_small.apply {
                text = nativeAd.headline
                visibility = View.VISIBLE
            }
        } else {
            adView.ad_headline_small.visibility = View.GONE
        }

        if(nativeAd.advertiser != null) {
            adView.ad_attribution_small.text = nativeAd.advertiser
            adView.ad_attribution_small.visibility = View.VISIBLE
        } else {
            adView.ad_attribution_small.visibility = View.GONE
        }
        adView.setNativeAd(nativeAd)
    }


    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = if(connectivityManager != null) {
            connectivityManager.activeNetworkInfo
        } else null
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun isInternetAvailable(): Boolean {
            val policy = StrictMode.ThreadPolicy.Builder()
                .permitAll().build()
            StrictMode.setThreadPolicy(policy)
            try {
                val urlc: HttpURLConnection =
                    URL("https://www.google.com/").openConnection() as HttpURLConnection
                urlc.setRequestProperty("User-Agent", "Test")
                urlc.setRequestProperty("Connection", "close")
                urlc.connectTimeout = 500 //choose your own timeframe
                urlc.readTimeout = 500 //choose your own timeframe
                urlc.connect()
                return urlc.responseCode == 200
            } catch (e: IOException) {
                return false //connectivity exists, but no internet.
            }

    }
    private const val mSharedPreferenceName = "ratio_data"
    private const val mRatioKey = "ratio_key"


    fun checkStorageSpace(listFilePath:ArrayList<String>) :Boolean{
        var totalFileLength = 0L
        for(index in 0 until listFilePath.size) {
            val file = File(listFilePath[index])
            if(file.exists()) {
                val fileLength = file.length()
                totalFileLength += fileLength
            }
        }

        val currentFreeSpace = getAvailableSpaceInMB()*1024*1024

        Logger.e("currentFreeSpace = $currentFreeSpace   totalFileLength = $totalFileLength")
        if(currentFreeSpace > (totalFileLength*2)) return true
        return false
    }

     fun convertSecondsToTime(seconds: Int): String {
        val timeStr: String
        var hour = 0
        var minute = 0
        var second = 0
        if (seconds <= 0) {
            return "00:00"
        } else {
            minute = seconds.toInt() / 60
            if (minute < 60) {
                second = seconds.toInt() % 60
                timeStr =
                    "00:" + unitFormat(minute) + ":" + unitFormat(
                        second
                    )
            } else {
                hour = minute / 60
                if (hour > 99) return "99:59:59"
                minute %= 60
                second = (seconds - hour *3600 - minute *60).toInt()
                timeStr =
                    unitFormat(hour) + ":" + unitFormat(
                        minute
                    ) + ":" + unitFormat(second)
            }
        }
        return timeStr
    }
    private fun unitFormat(i: Int): String? {
        return if (i in 0..9) {
            "0$i"
        } else {
            "" + i
        }
    }


}