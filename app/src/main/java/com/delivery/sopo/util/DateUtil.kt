package com.delivery.sopo.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtil
{
    const val DATE_TIME_TYPE_DEFAULT = "yyyy-MM-dd HH:mm:ss"
    const val DATE_TIME_TYPE_AUTH_EXPIRED = "yyyy.MM.dd HH:mm:ss"
    const val DATE_TIME_TYPE_PROGRESSES = "yy/MM/dd HH:mm:ss"
    const val DATE_TYPE_PROGRESSES = "yy/MM/dd"
    const val TIME_TYPE_PROGRESSES = "yy/MM/dd"
    const val TIMESTAMP_TYPE_AUTH_EXPIRED = "yyyy-MM-dd'T'HH:mm:ssX"
    const val TIMESTAMP_TYPE_TIME = "HH:mm"

    const val DATE_TYPE_KOREAN_SEMI = "yy년 MM월"
    const val DATE_TYPE_yyyyMM = "yyyyMM"
    const val DATE_TYPE_yyyy = "yyyy"
    const val DATE_TYPE_MM = "MM"
    const val DATE_TYPE_KOREAN_FULL = "yyyy년 MM월"

    fun getCurrentDateDay2(): String
    {
        val currentMillis = System.currentTimeMillis()
        val date = Date(currentMillis)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        return sdf.format(date)
    }


    fun getCurrentDateDay(): Long
    {
        val currentMillis = System.currentTimeMillis()
        val date = Date(currentMillis)
        val sdf = SimpleDateFormat(DATE_TYPE_PROGRESSES, Locale.KOREAN)
        val dateString = sdf.format(date)
        return sdf.parse(dateString).time
    }

    fun getCurrentDate(pattern: String = DATE_TIME_TYPE_DEFAULT): String
    {
        val currentMillis = System.currentTimeMillis()
        val date = Date(currentMillis)
        val sdf = SimpleDateFormat(pattern, Locale.KOREAN)
        return sdf.format(date)
    }

    fun getCurrentDate(): Date
    {
        val currentMilliseconds = System.currentTimeMillis()
        return Date(currentMilliseconds)
    }

    fun convertDate(dateString: String, pattern: String = DATE_TIME_TYPE_DEFAULT): Date?
    {
        return try
        {
            val sdf = SimpleDateFormat(pattern, Locale.KOREAN)
            val date: Date = sdf.parse(dateString) ?: return null
            return date
        }
        catch(e: Exception)
        {
            e.printStackTrace()
            null
        }
    }

    fun convertDate(dateMilliseconds: Long, pattern: String = DATE_TIME_TYPE_DEFAULT): String?
    {
        return try
        {
            val date = Date(dateMilliseconds)
            val sdf = SimpleDateFormat(pattern, Locale.KOREAN)
            return sdf.format(date)
        }
        catch(e: Exception)
        {
            e.printStackTrace()
            null
        }
    }

    fun changeDateFormat(date: String, oldPattern: String, newPattern: String): String?
    {
        return try
        {
            val oldSdf = SimpleDateFormat(oldPattern, Locale.KOREAN)
            val newSdf = SimpleDateFormat(newPattern, Locale.KOREAN)

            val oldDate = oldSdf.parse(date) ?: return null
            newSdf.format(oldDate)
        }
        catch(e: Exception)
        {
            e.printStackTrace()
            null
        }
    }

    fun getCurrentYear(): String
    {
        val calendar: Calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR).toString()
    }

    fun checkDateFormat(date: String, pattern: String = DATE_TIME_TYPE_DEFAULT): Boolean
    {
        return try
        {
            val sdf = SimpleDateFormat(pattern, Locale.KOREAN)
            sdf.parse(date) != null
        }
        catch(e: Exception)
        {
            e.printStackTrace()
            false
        }
    }

    /**
     *  true refreshToken의 만료일이 1주일 이하일 경우
     *  false 초과일 경우
     */
    fun isExpiredDateWithinAWeek(expiredDateString: String): Boolean
    {
//        SopoLog.i("isExpiredDateWithinAWeek() 호출")

        // 1. 현재 시간
        val currentMilliSeconds = System.currentTimeMillis() // 2. O-Auth 만료 기간
        val sdf = SimpleDateFormat(DATE_TIME_TYPE_DEFAULT, Locale.KOREAN)
        val expiredDate = sdf.parse(expiredDateString) ?: return false

//        SopoLog.d("현재시간 [$currentMilliSeconds]")
//        SopoLog.d("만료기한[${expiredDate.time}] [형태:$expiredDate]")

        val weekMilliSeconds = 1000 * 60 * 60 * 24 * 7

        val remainMilliSeconds = expiredDate.time - currentMilliSeconds

        return remainMilliSeconds <= weekMilliSeconds
    }

    fun getSubscribedTime(hour: Int, minutes: Int): String
    {
        val topicHour = if(minutes == 0)
        {
            hour + 1
        }
        else hour
        return topicHour.toString().padStart(2, '0').padEnd(4, '0')
    }

    /*// dateTime => yyyy-MM-dd'T'HH"mm:ss.SSS'Z -> yyyy-MM-dd HHmm
    fun changeDateFormat(dateTime: String): String
    {
        val oldFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        oldFormat.timeZone = TimeZone.getTimeZone("KST")
        val newFormat = SimpleDateFormat("yy/MM/dd HH:mm:ss")

        return try
        {
            val oldDate = oldFormat.parse(dateTime)
            newFormat.format(oldDate)
        }
        catch(e: ParseException)
        {
            SopoLog.e(msg = "Data Format Change Error", e = e)
            throw e
        }
    }*/

    /*    @SuppressLint("SimpleDateFormat")
    fun changeDateToMilli(date: String, format: String = "yyyy.MM.dd HH:mm:ss"): Long
    {
        try
        {
            val sdf = SimpleDateFormat(format)
            val parseDate = sdf.parse(date)
            return parseDate?.time ?: throw NullPointerException()
        }
        catch(e: Exception)
        {
            throw e
        }

    fun changeCalendarToDateTime(calendar: Calendar): String
    {
        return "${calendar.get(Calendar.YEAR)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DATE)} " +
                "${String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))}:${String.format("%02d", calendar.get(Calendar.MINUTE))}"
    }
    }*/

}

/*
fun main()
{

    val data = "2022-06-05T19:00:02.606+09:00[Asia/Seoul]"
    val zoneId = ZoneId.of("Asia/Seoul")
    println("DateUtil.changeDateFormat() => ${DateUtil.changeDateFormat(data, DateUtil.TIMESTAMP_TYPE_AUTH_EXPIRED, DateUtil.DATE_TIME_TYPE_DEFAULT)}")
}*/
