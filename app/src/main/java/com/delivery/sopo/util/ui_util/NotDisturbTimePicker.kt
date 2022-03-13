package com.delivery.sopo.util.ui_util

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.marginLeft
import androidx.core.view.updateLayoutParams
import com.delivery.sopo.util.SizeUtil
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotDisturbTimePicker(context: Context, attrs: AttributeSet? = null): TimePicker(context, attrs)
{
    init
    {
        val linearLayout: LinearLayout = getChildAt(0) as LinearLayout

        for(index in 0..linearLayout.childCount)
        {
            val childLinearLayout = linearLayout.getChildAt(index) as LinearLayout? ?: continue

            for(childIndex in 0..childLinearLayout.childCount)
            {
                val view = childLinearLayout.getChildAt(childIndex)

                if(view is NumberPicker)
                {

                    val numberPicker = view as NumberPicker

                    when(childIndex)
                    {
                        0->
                        {
                            setHourFormat(numberPicker)
                        }
                        2->
                        {
                            setMinuteFormat(numberPicker)
                        }
                    }

                }
                else if(view is TextView)
                {
                    val originTextView = view as TextView

                    val tvParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    tvParam.leftMargin = SizeUtil.changeDpToPx(originTextView.context, 33.0f)
                    tvParam.rightMargin = SizeUtil.changeDpToPx(originTextView.context, 33.0f)
                    tvParam.gravity = Gravity.CENTER
                    originTextView.layoutParams = tvParam
                }
            }

        }
    }

    fun setHourFormat(numberPicker: NumberPicker, timeInterval: Int = DEFAULT_INTERVAL){
        try {
            numberPicker.apply {
                descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                displayedValues = getDisplayedValueWithHour(timeInterval)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setMinuteFormat(numberPicker: NumberPicker, timeInterval: Int = DEFAULT_INTERVAL){
        try
        {
            numberPicker.apply {
                descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                minValue = MINUTES_MIN
                maxValue = MINUTES_MAX / timeInterval - 1
                displayedValues = getDisplayedValueWithMinute()
            }
        }
        catch(e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun getDisplayedValueWithHour(timeInterval: Int = DEFAULT_INTERVAL): Array<String>
    {
        val hourArray = ArrayList<String>()
        for (i in 0 until HOUR_MAX) {
            hourArray.add(i.toString().padStart(2, '0') + "시")
//            hourArray.add(i.toString())
        }

        return hourArray.toTypedArray()
    }

    private fun getDisplayedValueWithMinute(timeInterval: Int = DEFAULT_INTERVAL): Array<String>
    {
        val minutesArray = ArrayList<String>()
        for (i in 0 until MINUTES_MAX step timeInterval) {
            minutesArray.add(i.toString().padEnd(2, '0') + "분")
        }

        return minutesArray.toTypedArray()
    }

    companion object{
        const val DEFAULT_INTERVAL = 10

        const val HOUR_MIN = 0
        const val HOUR_MAX = 24

        const val MINUTES_MIN = 0
        const val MINUTES_MAX = 60
    }

}