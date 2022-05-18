package com.delivery.sopo.util

import android.annotation.SuppressLint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtil
{

    fun getCurrentYear(): String
    {
        val calendar: Calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR).toString()
    }

    fun getIntToMilliSeconds(seconds: Int): Long
    {
        val currentTimeMilliSeconds = System.currentTimeMillis()
        val secondsToMilliSeconds = seconds * 1000
        return currentTimeMilliSeconds + secondsToMilliSeconds
    }

    // dateTime => yyyy-MM-dd'T'HH"mm:ss.SSS'Z -> yyyy-MM-dd HHmm
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
    }

    @SuppressLint("SimpleDateFormat")
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
        val newFormat = SimpleDateFormat("yy년 MM월", Locale.KOREA).format(Date(date.time)) ?: return null
        return newFormat
    }

    fun changeCalendarToDateTime(calendar: Calendar): String
    {
        return "${calendar.get(Calendar.YEAR)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DATE)} " +
                "${String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))}:${String.format("%02d", calendar.get(Calendar.MINUTE))}"
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
        val expiredDateToMilliSeconds = changeDateToMilli(expiredDate)

        SopoLog.d("현재시간 [$currentMilliSeconds]")
        SopoLog.d("만료기한[$expiredDateToMilliSeconds] [형태:$expiredDate]")

        val weekMilliSeconds = 1000 * 60 * 60 * 24 * 7

        val remainMilliSeconds = expiredDateToMilliSeconds - currentMilliSeconds

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
}