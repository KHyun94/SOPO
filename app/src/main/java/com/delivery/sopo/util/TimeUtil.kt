package com.delivery.sopo.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

object TimeUtil
{
    @SuppressLint("SimpleDateFormat")
    fun getTime(): String
    {
        val date: Date = Date()
        val sdf: SimpleDateFormat = SimpleDateFormat("HHmm")
        return sdf.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateTime(): String
    {
        val date: Date = Date()
        val sdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    fun convertToFormat(milliseconds:Long): String
    {
        val date: Date = Date()
        val sdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    fun getDate(): String{
        val date: Date = Date()
        val sdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    fun getYYYYMMDate(): String{
        val date: Date = Date()
        val sdf: SimpleDateFormat = SimpleDateFormat("yyyyMM")
        return sdf.format(date)
    }
}