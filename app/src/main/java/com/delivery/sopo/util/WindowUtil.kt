package com.delivery.sopo.util

import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.delivery.sopo.R

object WindowUtil
{
    fun setWindowStatusBarColor(activity:FragmentActivity, @ColorRes colorRes: Int){
        activity.window.statusBarColor = ContextCompat.getColor(activity, colorRes)
    }


}