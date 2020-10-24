package com.delivery.sopo.util

import android.app.Activity
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DrawableRes
import com.delivery.sopo.SOPOApp


object OtherUtil
{

    fun hideKeyboardSoft(act: Activity)
    {
        val inputMethodManager =
            act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(act.currentFocus!!.windowToken, 0)
    }

    fun getDeviceID(context: Context): String
    {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
    }

    fun getResourceExtension(@DrawableRes res: Int): String?
    {

        val typedValue = TypedValue()

        Log.d("LOG.SOPO", "이미지 리소스 :->")

        SOPOApp.INSTANCE.resources.getValue(res, typedValue, true)

        val tmp = typedValue.string.toString()

        val split = tmp.split(".")

        return if (split.size > 0)
        {
            split[split.size - 1]
        }
        else
        {
            null
        }
    }
}