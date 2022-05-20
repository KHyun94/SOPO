package com.delivery.sopo.util

import android.annotation.SuppressLint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtil
{
    const val DATE_TIME_TYPE_DEFAULT = "yyyy-MM-dd HH:mm:ss"
    const val DATE_TIME_TYPE_AUTH_EXPIRED = "yyyy.MM.dd HH:mm:ss"
    const val DATE_TIME_TYPE_PROGRESSES = "yy/MM/dd HH:mm:ss"
    const val TIMESTAMP_TYPE_AUTH_EXPIRED = "yyyy-MM-dd'T'HH:mm:ssX"

    const val DATE_TYPE_KOREAN = "yy년 MM월"

    fun getCurrentDate(pattern: String = DATE_TIME_TYPE_DEFAULT): Date?
    {
        val currentMillis = System.currentTimeMillis()
        val date = Date(currentMillis)
        val sdf = SimpleDateFormat(pattern, Locale.KOREAN)
        return sdf.parse(date.toString())
    }

    fun convertDate(date: String, pattern: String = DATE_TIME_TYPE_DEFAULT): Date?
    {
        val sdf = SimpleDateFormat(pattern, Locale.KOREAN)
        return sdf.parse(date)
    }

    fun changeDateFormat(date: String, oldPattern: String, newPattern: String): Date?
    {
        val oldSdf = SimpleDateFormat(oldPattern, Locale.KOREAN)
        val newSdf = SimpleDateFormat(newPattern, Locale.KOREAN)

        val oldDate = oldSdf.parse(date) ?: return null
        val newDateString = newSdf.format(oldDate)
        return newSdf.parse(newDateString)
    }

    fun getCurrentYear(): String
    {
        val calendar: Calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR).toString()
    }


    fun changeDateTime(date: String): Date?
    {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(date)
    }

    fun changeCalendarToDate(calendar: Calendar): String
    {
        return "${calendar.get(Calendar.YEAR)}/${calendar.get(Calendar.MONTH) + 1}"
    }

    fun toDateKorTime(date: String): String?
    {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(date) ?: return null
        val newFormat =
            SimpleDateFormat("yy년 MM월", Locale.KOREA).format(Date(date.time)) ?: return null
        return newFormat
    }

    fun checkDateFormat(date: String, pattern: String = DATE_TIME_TYPE_DEFAULT): Boolean
    {
        val sdf = SimpleDateFormat(pattern, Locale.KOREAN)
        return sdf.parse(date) != null
    }


    fun calculateDiffPresentDate(targetDate: Date): String
    {
        val now = System.currentTimeMillis()
        val target = targetDate.time

        val diffMillis = (now - target)
        val diffHour = diffMillis / (1000 * 60 * 60)

        return when
        {
            diffHour < 1 ->
            {
                "최근 1시간 내 업데이트"
            }
            diffHour in 1..23 ->
            {
                "${diffHour}시간 전 업데이트"
            }
            diffHour in 24..743 ->
            {
                val diffDay = diffHour / 24
                "${diffDay}일 전 업데이트"
            }
            diffHour in 744..8927 ->
            {
                val diffMonth = diffHour / (24 * 31)
                "${diffMonth}개월 전 업데이트"
            }
            diffHour > 8927 ->
            {
                val diffYear = diffHour / (24 * 31 * 12)
                "${diffYear}년 전 업데이트"
            }
            else ->
            {
                "업데이트 일자 확인 불가"
            }
        }


    }

    /**
     *  true refreshToken의 만료일이 1주일 이하일 경우
     *  false 초과일 경우
     */
    fun isExpiredDateWithinAWeek(expiredDate: String): Boolean
    {
        SopoLog.i("isExpiredDateWithinAWeek() 호출")

        // 1. 현재 시간
        val currentMilliSeconds = System.currentTimeMillis() // 2. O-Auth 만료 기간
        val expiredDateToMilliSeconds = convertDate(expiredDate, DATE_TIME_TYPE_AUTH_EXPIRED) ?: return false

        SopoLog.d("현재시간 [$currentMilliSeconds]")
        SopoLog.d("만료기한[$expiredDateToMilliSeconds] [형태:$expiredDate]")

        val weekMilliSeconds = 1000 * 60 * 60 * 24 * 7

        val remainMilliSeconds = expiredDateToMilliSeconds.time - currentMilliSeconds

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