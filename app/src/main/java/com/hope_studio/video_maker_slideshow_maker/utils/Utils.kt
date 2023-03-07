package com.hope_studio.video_maker_slideshow_maker.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.net.ConnectivityManager
import android.os.Environment
import android.os.StatFs
import android.os.StrictMode
import com.hope_studio.video_maker_slideshow_maker.R
import com.hope_studio.video_maker_slideshow_maker.application.VideoMakerApplication
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
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