package com.delivery.sopo.util

import android.util.Log

object SopoLog
{
    private val TAG = "[LOG.SOPO"

    fun v(tag: String? = null, msg: String)
    {
        val _tag = if (tag == null) "${TAG}.v]" else "${TAG}.${tag}.v]"
        Log.v(_tag, msg)
    }

    fun i(tag: String? = null, msg: String)
    {
        val _tag = if (tag == null) "${TAG}.i]" else "${TAG}.${tag}.i]"
        Log.i(_tag, msg)
    }

    fun d(tag: String? = null, msg: String )
    {
        val _tag = if (tag == null) "${TAG}.d]" else "${TAG}.${tag}.d]"
        Log.d(_tag, msg)
    }

    fun w(tag: String? = null, msg: String)
    {
        val _tag = if (tag == null) "${TAG}.w]" else "${TAG}.${tag}.w]"
        Log.v(_tag, msg)
    }

    fun e(tag: String? = null, msg: String, e: Throwable? = null)
    {
        val _tag = if (tag == null) "${TAG}.e]" else "${TAG}.${tag}.e]"
        if(e == null) Log.e(_tag, "$msg\n${e.toString()}")
        else Log.e(_tag, "$msg\n${e.toString()}", e)
    }

}