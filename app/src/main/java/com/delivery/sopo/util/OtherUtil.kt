package com.delivery.sopo.util

import android.app.Activity
import android.content.Context
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import java.io.File


object OtherUtil
{
    fun changeTest(v: TextView)
    {
        val text = v.text.toString()

        SopoLog.d("Text >>> ${text}")
        val builder = SpannableStringBuilder(v.text, 0, text.length-1)
        builder.setSpan(StyleSpan(R.font.roboto_bold), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(StyleSpan(R.font.spoqa_han_sans_neo_bold), 3, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(StyleSpan(R.font.spoqa_han_sans_neo_light), 6, text.length-1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        v.text = builder
    }


    fun hideKeyboardSoft(act: Activity)
    {
        val inputMethodManager =
            act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(act.currentFocus?.windowToken, 0)
    }

    fun getDeviceID(context: Context): String
    {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
    }

    fun getResourceExtension(@DrawableRes res: Int): String?
    {
        val typedValue = TypedValue()

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

    fun clearCache(context: Context)
    {
        val cacheDirFile = context.cacheDir

        if (cacheDirFile != null && cacheDirFile.isDirectory)
        {
            clearCacheFiles(cacheDirFile)
        }
    }

    fun clearCacheFiles(cacheDirFile: File?)
    {
        if (cacheDirFile == null || cacheDirFile.isFile) return

        for (file in cacheDirFile.listFiles())
        {
            if (file.isFile)
            {
                if (file.exists()) file.delete()
            }
            else
            {
                clearCacheFiles(file)
            }
        }
    }

    fun getRandomInteger(length: Int):Int
    {
        val charset = "123456789"
        return  (1..length).map { charset.random() }.joinToString("").toInt()
    }

}