package com.delivery.sopo.util

import android.util.Log
import java.lang.Exception

object SopoLog
{
    val TAG = "LOG.SOPO"

    fun v(str : String, tag : String?)
    {
        Log.v(tag?: "$TAG.v", str)
    }

    fun i(str : String, tag : String?)
    {
        Log.i(tag?: "$TAG.i", str)
    }
    fun d(str : String, tag : String? = null)
    {
        Log.d(tag?: "$TAG.d", str)
    }

    fun w(str : String, tag : String?)
    {
        Log.v(tag?: "$TAG.d", str)
    }

    fun e(str : String, e : Throwable?, tag : String? = null)
    {
        Log.e(tag?: "$TAG.e", "$str\n${e.toString()}", e)
    }

}