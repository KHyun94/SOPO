package com.delivery.sopo.util

import com.delivery.sopo.extensions.toMilliSeconds
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtil
{
    fun getIntToMilliSeconds(seconds: Int): Long
    {
        val currentTimeMilliSeconds = System.currentTimeMillis()
        val secondsToMilliSeconds = seconds * 1000
        return currentTimeMilliSeconds + secondsToMilliSeconds
    }

    // dateTime => yyyy-MM-dd'T'HH"mm:ss.SSS'Z -> yyyy-MM-dd HHmm
    fun changeDateFormat(dateTime: String): String?
    {
        val oldFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        oldFormat.timeZone = TimeZone.getTimeZone("KST")
        val newFormat = SimpleDateFormat("yy/MM/dd HH:mm:ss")

        return try
        {
            val oldDate = oldFormat.parse(dateTime)
            newFormat.format(oldDate)
        }
        catch (e: ParseException)
        {
            SopoLog.e(msg = "Data Format Change Error", e = e)
            null
        }
    }

    fun compareCurrentDate(date: String): Boolean
    {
        if(!ValidateUtil.isValidateDateFormat(date)) return false

        val baseMilliSeconds = date.toMilliSeconds()!!
        val currentMilliSeconds = System.currentTimeMillis()

        return currentMilliSeconds <= baseMilliSeconds
    }
}