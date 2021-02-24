package com.delivery.sopo.extensions

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
fun Long.toDate(): String
{
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    val date = Date(this)
    return sdf.format(date)
}