package com.delivery.sopo.util

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.orhanobut.logger.Logger
import java.lang.StringBuilder

object SopoLog
{
    val TAG = "SOPO_"

    fun v(msg: String)
    {
        Logger.t(TAG + "VERB").v(msg)
        FirebaseCrashlytics.getInstance().log("[VERB] $msg")
    }

    fun i(msg: String)
    {
        Logger.t(TAG+ "INFO").i(msg)
        FirebaseCrashlytics.getInstance().log("[INFO] $msg")
    }

    @JvmStatic
    fun d(msg: String)
    {
        Logger.t(TAG + "DEV").d(msg)
        FirebaseCrashlytics.getInstance().log("[DEV] $msg")
    }

    fun w(msg: String)
    {
        Logger.t(TAG + "WARN").w(msg)
        FirebaseCrashlytics.getInstance().log("[WARN] $msg")
    }

    fun e(msg: String, e: Throwable? = null)
    {
        Logger.t(TAG + "ERROR").e(e, msg)
        e?.printStackTrace()
        FirebaseCrashlytics.getInstance().log("[ERROR] $msg | ${e?.printStackTrace()}")
    }

    fun api(msg: String)
    {
        Logger.t(TAG + "NETWORK").d(msg)
    }
}