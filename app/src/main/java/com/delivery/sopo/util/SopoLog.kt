package com.delivery.sopo.util

import android.util.Log
import java.lang.Exception

object SopoLog
{
    val TAG = "[LOG.SOPO"

    fun v(str : String, tag : String?)
    {
        val _tag = if(tag == null) "${TAG}.v]" else "${TAG}.${tag}.v]"
        Log.v(_tag, str)
    }

    fun i(str : String, tag : String?)
    {
        val _tag = if(tag == null) "${TAG}.i]" else "${TAG}.${tag}.i]"
        Log.i(_tag, str)
    }
    fun d(str : String, tag : String? = null)
    {
        val _tag = if(tag == null) "${TAG}.d]" else "${TAG}.${tag}.d]"
        Log.d(_tag, str)
    }

    fun w(str : String, tag : String?)
    {
        val _tag = if(tag == null) "${TAG}.w]" else "${TAG}.${tag}.w]"
        Log.v(_tag, str)
    }

    fun e(str : String, e : Throwable?, tag : String? = null)
    {
        val _tag = if(tag == null) "${TAG}.e]" else "${TAG}.${tag}.e]"
        Log.e(_tag, "$str\n${e.toString()}", e)
    }

}