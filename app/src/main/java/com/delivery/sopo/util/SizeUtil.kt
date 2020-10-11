package com.delivery.sopo.util

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ListAdapter

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

    fun changeSpToPx(context: Context, sp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics
        ).toInt()
    }

    fun measureContentHeight(context: Context, listAdapter: ListAdapter): Int {
        var mMeasureParent: ViewGroup? = null
        var maxHeight = 0
        var itemView: View? = null
        var itemType = 0
        val adapter: ListAdapter = listAdapter
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val count: Int = adapter.count
        for (i in 0 until count) {
            val positionType: Int = adapter.getItemViewType(i)
            if (positionType != itemType) {
                itemType = positionType
                itemView = null
            }
            if (mMeasureParent == null) {
                mMeasureParent = FrameLayout(context)
            }
            itemView = adapter.getView(i, itemView, mMeasureParent)
            itemView.measure(widthMeasureSpec, heightMeasureSpec)
            val itemWidth = itemView.measuredWidth
            val itemHeight = itemView.measuredHeight

            if (itemHeight > maxHeight) {
                maxHeight = itemHeight
            }
        }
        return maxHeight
    }
}
