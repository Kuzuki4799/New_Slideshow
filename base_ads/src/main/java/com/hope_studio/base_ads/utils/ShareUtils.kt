package com.hope_studio.base_ads.utils

import android.app.Activity
import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.ArrayList

object ShareUtils {

    fun checkFirstFun(context: Context?): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return if (preferences.getBoolean("first_run", true)) {
            preferences.edit().putBoolean("first_run", false).apply()
            true
        } else false
    }

    fun <FOP> writeFileJson(context: Context, name: String, data: FOP) {
        try {
            val gSon = Gson()
            val fileOut = context.openFileOutput(name, Activity.MODE_PRIVATE)
            val osw = OutputStreamWriter(fileOut)
            osw.write(gSon.toJson(data))
            osw.flush()
            osw.close()
        } catch (e: FileNotFoundException) {
            BLog.e("write error: ${e.message}")
        } catch (e: IOException) {
            BLog.e("write error: ${e.message}")
        }
    }

    fun <FIP> readFileJson(context: Context, name: String, typeData: Class<FIP>): FIP {
        val fin = context.openFileInput(name)
        val isr = InputStreamReader(fin)
        var inputBuffer = CharArray(10)
        var charRead: Int
        var readString = ""
        while (isr.read(inputBuffer).also { charRead = it } > 0) {
            readString += String(inputBuffer, 0, charRead)
            inputBuffer = CharArray(10)
        }
        return Gson().fromJson(readString, typeData)
    }

    fun <FIP> readFileJsonArray(
        context: Context,
        name: String,
        typeData: Class<FIP>,
    ): ArrayList<FIP> {
        val list = ArrayList<FIP>()
        val fin = context.openFileInput(name)
        val isr = InputStreamReader(fin)
        var inputBuffer = CharArray(10)
        var charRead: Int
        var readString = ""
        while (isr.read(inputBuffer).also { charRead = it } > 0) {
            readString += String(inputBuffer, 0, charRead)
            inputBuffer = CharArray(10)
        }
        val gSon = Gson()
        val array = JsonParser().parse(readString).asJsonArray
        for (jsonElement in array) list.add(gSon.fromJson(jsonElement, typeData))
        return list
    }

    fun <T> put(context: Context, key: String, value: T) {
        try {
            val gSon = Gson()
            val fileOut = context.openFileOutput(key, Activity.MODE_PRIVATE)
            val osw = OutputStreamWriter(fileOut)
            osw.write(gSon.toJson(value))
            osw.flush()
            osw.close()
        } catch (e: FileNotFoundException) {
            BLog.e("write error: ${e.message}")
        } catch (e: IOException) {
            BLog.e("write error: ${e.message}")
        }
    }

    operator fun <T> get(context: Context, key: String, type: Class<T>): T? {
        return try {
            val fin = context.openFileInput(key)
            val isr = InputStreamReader(fin)
            var inputBuffer = CharArray(10)
            var charRead: Int
            var readString = ""
            while (isr.read(inputBuffer).also { charRead = it } > 0) {
                readString += String(inputBuffer, 0, charRead)
                inputBuffer = CharArray(10)
            }
            Gson().fromJson(readString, type)
        } catch (e: Exception) {
            null
        }
    }

    fun <T> putArrayGson(context: Context?, key: String, value: ArrayList<T>?) {
        if (value == null || context == null) return
        val json = Gson().toJson(value, object : TypeToken<ArrayList<T>?>() {}.type)
        val className = ArrayList::class.java.name + key
        println("xxxxxx$className")
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(className, json)
        editor.apply()
    }

    fun <T> getArrayGson(context: Context, t: Class<T>): ArrayList<T> {
        val list = ArrayList<T>()
        try {
            val className = ArrayList::class.java.name + t.name
            val json =
                PreferenceManager.getDefaultSharedPreferences(context).getString(className, "")
            println("getArray:$json")
            val gSon = Gson()
            val array = JsonParser().parse(json).asJsonArray
            for (jsonElement in array) list.add(gSon.fromJson(jsonElement, t))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun <T> getArray(context: Context, t: Class<T>): ArrayList<T> {
        val list = ArrayList<T>()
        try {
            val fin = context.openFileInput(t.name)
            val isr = InputStreamReader(fin)
            var inputBuffer = CharArray(10)
            var charRead: Int
            var readString = ""
            while (isr.read(inputBuffer).also { charRead = it } > 0) {
                readString += String(inputBuffer, 0, charRead)
                inputBuffer = CharArray(10)
            }
            val gSon = Gson()
            val array = JsonParser().parse(readString).asJsonArray
            for (jsonElement in array) list.add(gSon.fromJson(jsonElement, t))
        } catch (e: Exception) {
        }
        return list
    }

    fun putString(context: Context?, key: String?, value: String?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (value == null) preferences.edit().remove(key).apply()
        else preferences.edit().putString(key, value).apply()
    }

    fun getString(context: Context?, key: String?, defValue: String?): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return if (preferences.contains(key)) {
            preferences.getString(key, defValue)
        } else defValue
    }

    fun putInt(context: Context?, key: String?, value: Int) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putInt(key, value).apply()
    }

    fun getInt(context: Context?, key: String?, defValue: Int): Int {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return if (preferences.contains(key)) {
            preferences.getInt(key, defValue)
        } else defValue
    }

    fun putLong(context: Context?, key: String?, value: Long) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putLong(key, value).apply()
    }

    fun getLong(context: Context?, key: String?, defValue: Long): Long {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return if (preferences.contains(key)) {
            preferences.getLong(key, defValue)
        } else defValue
    }

    fun putBoolean(context: Context?, key: String?, value: Boolean) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(context: Context?, key: String?, defValue: Boolean): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return if (preferences.contains(key)) {
            preferences.getBoolean(key, defValue)
        } else defValue
    }

    fun putFloat(context: Context?, key: String?, value: Float) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putFloat(key, value).apply()
    }

    fun getFloat(context: Context?, key: String?, defValue: Float): Float {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return if (preferences.contains(key)) {
            preferences.getFloat(key, defValue)
        } else defValue
    }

    fun clearData(context: Context?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().clear().apply()
    }
}