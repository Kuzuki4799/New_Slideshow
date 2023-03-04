package com.hope_studio.base_ads.utils

import android.annotation.SuppressLint
import android.content.Context
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

object TimeUtils {

    private var WEEK = "E"
    var DATE_FORMAT = "dd/MM/yyyy"
    var HOUR_MIN = "HH:mm"
    var HOUR_MIN_12 = "hh:mm a"
    private var MONTH_DAY = "MM/dd"
    private var WEEK_TIME = "E HH:mm"
    private var DATE_TIME = "dd"
    private var MM_TIME = "MM"
    private var YY_TIME = "yyyy"
    private var WEEK_TIME_12 = "E hh:mm a"
    const val dayFormat = "yyyyMMdd_HHmmss"
    var WEEK_DATE_MONTH = "E, MM dd"
    var WEEK_DATE_MONTH_ = "EEEE, dd MMMM"
    const val DATE_FORMAT_TIME = "HH:mm:ss dd-MM-yyyy"
    val DATE_FORMAT_UTC = "yyyy-MM-dd HH:mm:ss'UTC'"
    val DATE_FORMAT_UTC_GLOBAL = "yyyy-MM-dd HH:mm:ss"
    private const val DATE_TIME_FORMAT = "HH:mm dd/MM/yyyy"

    const val FORMAT__ = "dd/MM/yyyy (EEEE)"
    const val FORMAT_ = "dd-MM-yyyy"

    @SuppressLint("SimpleDateFormat")
    fun formatTime(strTime: String, format: String): Date? {
        return SimpleDateFormat(format).parse(strTime)!!
    }

    @SuppressLint("SimpleDateFormat")
    fun formatLongToDateTime(timeLong: Long?, isUTC: Boolean, format: String): String {
        return if (isUTC) {
            formatUTCToLocalDateTime(timeLong, format)
        } else {
            SimpleDateFormat(format).format(Date(timeLong!!))
        }
    }

    fun parseDateFormatTimeUS(strDate: String): String {
        val format = SimpleDateFormat(DATE_FORMAT_UTC_GLOBAL, Locale.US)
        try {
            val date = format.parse(strDate)
            val dateFormat = SimpleDateFormat(HOUR_MIN, Locale.getDefault())
            return dateFormat.format(date!!)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null.toString()
    }

    fun parseDateFormatTimeUS12Hour(strDate: String): String {
        val format = SimpleDateFormat(DATE_FORMAT_UTC_GLOBAL, Locale.US)
        try {
            val date = format.parse(strDate)
            val dateFormat = SimpleDateFormat(HOUR_MIN_12, Locale.getDefault())
            return dateFormat.format(date!!)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null.toString()
    }

    fun parseDateFormatMonthDate(strDate: String): String {
        val format = SimpleDateFormat(DATE_FORMAT_UTC_GLOBAL, Locale.US)
        try {
            val date = format.parse(strDate)
            val dateFormat = SimpleDateFormat(MONTH_DAY, Locale.US)
            return dateFormat.format(date!!)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null.toString()
    }

    fun parseDateFormatUShowing(strDate: String, type: String): Date {
        val dateFormat = SimpleDateFormat(type, Locale.US)
        return dateFormat.parse(strDate)!!
    }

    fun formatLongToMonthDay(context: Context, timeLong: Int): String {
        val sdf = SimpleDateFormat(MONTH_DAY, Locale.getDefault())
        return sdf.format(Date(timeLong.toLong() * 1000))
    }


    @SuppressLint("SimpleDateFormat")
    fun formatTimeStampUTC(context: Context, timestamp: Long): String {
        val sdf = SimpleDateFormat(HOUR_MIN)
        return sdf.format(Date(timestamp * 1000))
    }

    @SuppressLint("SimpleDateFormat")
    fun formatTimeStampUTC12Hour(context: Context, timestamp: Long): String {
        val sdf = SimpleDateFormat(HOUR_MIN_12)
        return sdf.format(Date(timestamp * 1000))
    }

    @SuppressLint("SimpleDateFormat")
    fun formatLongUTC(context: Context, string: String, type: String): Long {
        val sdf = SimpleDateFormat(type)
        return sdf.parse(string).time
    }

    fun getGmtOffsetString(offsetMillis: Int): String? {
        var offsetMinutes = offsetMillis / 60000
        var sign = '+'
        if (offsetMinutes < 0) {
            sign = '-'
            offsetMinutes = -offsetMinutes
        }
        return String.format("GMT%c%02d:%02d", sign, offsetMinutes / 60, offsetMinutes % 60)
    }

    fun getCurrentTimezoneOffset(): String? {
        val tz = TimeZone.getDefault()
        val cal = GregorianCalendar.getInstance(tz)
        val offsetInMillis = tz.getOffset(cal.timeInMillis)
        var offset = String.format(
            "%02d:%02d", abs(offsetInMillis / 3600000), abs(offsetInMillis / 60000 % 60)
        )
        offset = "GMT" + (if (offsetInMillis >= 0) "+" else "-") + offset
        return offset
    }

    private fun formatLongToTimeWeek(context: Context, format: String, timeLong: Long?): String {
        val calendar = Calendar.getInstance()
        val tz = TimeZone.getDefault()
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.timeInMillis))
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(Date(timeLong!! * 1000))
    }

    @SuppressLint("SimpleDateFormat")
    fun formatLongToDateWeek(context: Context, timeLong: Int): String {
        val sdf = SimpleDateFormat(WEEK, Locale.US)
        return sdf.format(Date(timeLong.toLong() * 1000))
    }

    fun formatUTCToLocalDateTime(timeLong: Long?, format: String): String {
        val value = convertUTCToLocalTime(timeLong!!)
        val oldFormatter = SimpleDateFormat(format, Locale.getDefault())
        return oldFormatter.format(value)
    }

    fun formatTimeUTCToLocalTime(time: String, locale: Locale): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        var today = sdf.format(Date())
        today += " $time"

        val df = SimpleDateFormat(DATE_TIME_FORMAT, locale)
        df.timeZone = TimeZone.getTimeZone("UTC")
        val date: Date?
        return try {
            date = df.parse(today)
            val dfNew = SimpleDateFormat(HOUR_MIN, Locale.getDefault())
            dfNew.timeZone = TimeZone.getDefault()
            dfNew.format(date!!)
        } catch (e: ParseException) {
            e.printStackTrace()
            time
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getCurrentTime(format: String, zoneId: String): String {
        val time = SimpleDateFormat(format)
        time.timeZone = TimeZone.getTimeZone(zoneId)
        return time.format(Date())
    }

    @SuppressLint("SimpleDateFormat")
    fun formatCurrentTimeToLong(timeString: String, format: String, zoneId: String): Long {
        val sdf = SimpleDateFormat(format)
        try {
            sdf.timeZone = TimeZone.getTimeZone(zoneId)
            val dt = sdf.parse(timeString)
            return dt.time
        } catch (ex: ParseException) {
            ex.printStackTrace()
        }
        return 0
    }

    fun formatLocalTimeToTimeStamp(dateString: String): Long {
        var startDate: Long = 0
        try {
            val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val date = sdf.parse(dateString)
            startDate = date?.time!!
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return startDate
    }

    fun convertLocalTimeToUTC(timestampMs: Long): Long {
        val localZone = TimeZone.getDefault()
        val offset = localZone.getOffset(timestampMs).toLong()
        return timestampMs - offset
    }

    private fun convertUTCToLocalTime(timestampMs: Long): Long {
        val localZone = TimeZone.getDefault()
        val offset = localZone.getOffset(timestampMs).toLong()
        return timestampMs + offset
    }

    fun formatDuration(milliseconds: Long): String? {
        try {
            val second = milliseconds / 1000
            return if (milliseconds <= 0) {
                "00:00"
            } else {
                if (second in 0..59) {
                    val sec =
                        if (second % 60 < 10) "0" + second % 60 else (second % 60).toString()
                    "00:$sec"
                } else if (second > 60 && second < 60 * 60) {
                    val sec =
                        if (second % 60 < 10) "0" + second % 60 else (second % 60).toString()
                    val min =
                        if (second / 60 < 10) "0" + second / 60 else (second / 60).toString()
                    "$min:$sec"
                } else {
                    val hour =
                        if (second / 3600 < 10) "0" + second / 3600 else (second / 3600).toString()
                    val min: String = if (second % 3600 > 60) {
                        if (second % 3600 % 60 < 10) "0" + second % 3600 % 60 else (second % 3600 % 60).toString()
                    } else {
                        "00"
                    }
                    val sec =
                        if (second % 3600 < 10) "0" + second % 60 else (second % 60).toString()
                    "$hour:$min:$sec"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "00:00"
    }
}