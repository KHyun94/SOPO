package com.delivery.sopo.util

import com.delivery.sopo.extensions.toMilliSeconds
import org.jetbrains.annotations.TestOnly
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
    fun changeDateFormat(dateTime : String) : String?
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
            null
        }
    }

    fun compareCurrentDate(date : String) : Boolean
    {
        if (!ValidateUtil.isValidateDateFormat(date)) return false

        val baseMilliSeconds = date.toMilliSeconds()!!
        val currentMilliSeconds = System.currentTimeMillis()

        return currentMilliSeconds <= baseMilliSeconds
    }


    fun getSubscribedTime() : String
    {
        val calendar = Calendar.getInstance()

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)

        SopoLog.d(
            msg = """
            시 : $hour
            분 : $minutes
        """.trimIndent()
        )

        return if (minutes > 0)
        {
            val topicHour = hour.toString().padStart(2, '0').padEnd(4, '0')
            SopoLog.d(
                msg = """
            구독 시간 ${topicHour}
        """.trimIndent()
            )

            topicHour
        }
        else
        {
            val topicHour = (hour + 1).toString().padStart(2, '0').padEnd(4 , '0')
            SopoLog.d(
                msg = """
            구독 시간 ${topicHour}
        """.trimIndent()
            )

            topicHour
        }
    }
}