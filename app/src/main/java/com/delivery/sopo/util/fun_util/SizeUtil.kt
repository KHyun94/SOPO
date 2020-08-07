package com.delivery.sopo.util.fun_util

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.util.TypedValue
import android.view.WindowManager

object SizeUtil {
    // 디바이스 크기를 가져옵니다.
    fun getDeviceSize(act: Activity): Point {
        val windowManager = act.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    }

    fun changeDpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics
        ).toInt()
    }
}
