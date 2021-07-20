package com.delivery.sopo.util

import android.annotation.SuppressLint
import com.delivery.sopo.extensions.toMilliSeconds
import org.jetbrains.annotations.TestOnly
import java.lang.Exception
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtil
{
    fun getIntToMilliSeconds(seconds : Int) : Long
    {
        val currentTimeMilliSeconds = System.currentTimeMillis()
        val secondsToMilliSeconds = seconds * 1000
        return currentTimeMilliSeconds + secondsToMilliSeconds
    }

    // dateTime => yyyy-MM-dd'T'HH"mm:ss.SSS'Z -> yyyy-MM-dd HHmm
    fun changeDateFormat(dateTime : String) : String
    {
        val oldFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        oldFormat.timeZone = TimeZone.getTimeZone("KST")
        val newFormat = SimpleDateFormat("yy/MM/dd HH:mm:ss")

        return try
        {
            val oldDate = oldFormat.parse(dateTime)
            newFormat.format(oldDate)
        }
        catch (e : ParseException)
        {
            SopoLog.e(msg = "Data Format Change Error", e = e)
            throw e
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun changeDateToMilli(date: String, format: String = "yyyy.MM.dd HH:mm:ss"): Long
    {
        try
        {
            val sdf = SimpleDateFormat(format)
            val parseDate = sdf.parse(date)
            return parseDate?.time?:throw NullPointerException()
        }
        catch (e: Exception)
        {
            throw e
        }
    }

    /**
     *  true refreshToken의 만료일자가 현재 시간보다 작을 경우
     *  false 클 경우
     */
    fun isOverExpiredDate(expiredDate: String): Boolean
    {
        val currentMilliSeconds = System.currentTimeMillis()
        val expiredDateToMilliSeconds = changeDateToMilli(expiredDate)

        SopoLog.d("""isOverExpiredDate() call
            
            현재 >>> $currentMilliSeconds
            만료 >>> $expiredDateToMilliSeconds
            
        """.trimIndent())

        return currentMilliSeconds >= expiredDateToMilliSeconds
    }

    fun getSubscribedTime(hour: Int, minutes: Int) : String
    {
        val topicHour = if(minutes == 0) { hour + 1 } else hour
        return topicHour.toString().padStart(2, '0').padEnd(4, '0')
    }
}