package com.delivery.sopo.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtil
{
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

}