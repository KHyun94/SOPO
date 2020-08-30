package com.delivery.sopo.util.fun_util

import android.app.Activity
import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager


object OtherUtil {

    fun hideKeyboardSoft(act: Activity){
        val inputMethodManager = act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(act.currentFocus!!.windowToken, 0)
    }

    fun getDeviceID(context: Context): String {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
    }
}