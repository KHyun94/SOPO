package com.delivery.sopo.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.lang.StringBuilder

object SopoLog
{
    val TAG = "SOPO_LOG"

    fun v(msg: String)
    {
        Log.v(TAG, buildLogMsg("VERB", msg))
        FirebaseCrashlytics.getInstance().log("[VERB] $msg")
    }

    fun i(msg: String)
    {
        Log.i(TAG, buildLogMsg("INFO", msg))
        FirebaseCrashlytics.getInstance().log("[INFO] $msg")
    }

    @JvmStatic
    fun d(msg: String)
    {
        Log.d(TAG, buildLogMsg("DEV", msg))
        FirebaseCrashlytics.getInstance().log("[DEV] $msg")
    }

    fun w(msg: String)
    {
        Log.w(TAG, buildLogMsg("WARN", msg))
        FirebaseCrashlytics.getInstance().log("[WARN] $msg")
    }

    fun e(msg: String, e: Throwable? = null)
    {
        Log.e(TAG, buildLogMsg("ERROR", msg), e)
        FirebaseCrashlytics.getInstance().log("[ERROR] $msg | ${e?.printStackTrace()}")
    }

    fun api(msg: String)
    {
        Log.d(TAG, buildLogMsg("API", msg))
    }

    private fun buildLogMsg(type: String, message: String): String
    {
        val sb = StringBuilder()
        sb.append("[")
        sb.append(type)
        sb.append("] ")

        try
        {
            val ste = Thread.currentThread().stackTrace[4]

            if (ste != null)
            {
                sb.append("[")
                sb.append(ste.fileName.replace(".kt", ""))
                sb.append("::")
                sb.append(ste.methodName)
                sb.append("] ")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }

        sb.append(message)
        //        if(DEBUG_FLAG) appendLog(sb.toString());

        return sb.toString()
    }
}