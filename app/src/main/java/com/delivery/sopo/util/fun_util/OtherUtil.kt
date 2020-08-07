package com.delivery.sopo.util.fun_util

import android.content.Context
import android.provider.Settings

object OtherUtil {
    fun getDeviceID(context: Context): String {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
    }
}