package com.acatapps.videomaker.utils

import android.content.Context
import com.acatapps.videomaker.R
import com.acatapps.videomaker.application.VideoMakerApplication
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception

object RawResourceReader {

    fun readTextFileFromRawResource(context: Context, resourceId: Int): String {

        val inputStream = context.resources.openRawResource(resourceId)
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)

        var nextLine = ""
        val body = StringBuilder()
        try {

            while (bufferedReader.readLine().also { nextLine = it } != null) {
                body.append(nextLine)
                body.append("\n")
            }

        } catch (e:Exception) {

        }
        return body.toString()
    }

    fun readTextColorFile():ArrayList<String> {
        val textColorList = ArrayList<String>()
        val inputStream = VideoMakerApplication.getContext().resources.openRawResource(R.raw.color_list2)
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        var nextLine = ""
        try {

            while (bufferedReader.readLine().also { nextLine = it } != null) {
               textColorList.add(nextLine.trim())
            }

        } catch (e:Exception) {

        }
        return textColorList
    }

}