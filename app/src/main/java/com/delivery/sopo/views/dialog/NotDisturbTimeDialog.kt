package com.delivery.sopo.views.dialog

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.util.SopoLog
import com.google.android.material.tabs.TabLayout.*
import kotlinx.android.synthetic.main.set_not_disturb_time_dialog.*
import kotlinx.android.synthetic.main.set_not_disturb_time_dialog.view.*
import kotlin.math.abs

typealias DatePeriodCallback = (String, String) -> Unit

class NotDisturbTimeDialog : DialogFragment {

    private var parentActivity: Activity
    private lateinit var layoutView: View
    var callback : DatePeriodCallback
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"
    var startTimeList : List<String>
    var endTimeList : List<String>

    constructor(act: Activity, startTime : String = "00:00", endTime: String = "00:00", callback: DatePeriodCallback) : super() {
        this.parentActivity = act
        this.startTimeList = startTime.split(":")
        this.endTimeList = endTime.split(":")
        this.callback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layoutView = inflater.inflate(R.layout.set_not_disturb_time_dialog, container, false)
        setSetting()

        val tabs = layoutView.tabs
        tabs.addTab(tabs.newTab().setText("시작"))
        tabs.addTab(tabs.newTab().setText("종료"))

        tabs.addOnTabSelectedListener(object: OnTabSelectedListener
        {
            override fun onTabSelected(tab: Tab?)
            {
                when (tabs.selectedTabPosition)
                {
                    0 ->
                    {
                        constraint_start.visibility = VISIBLE
                        constraint_end.visibility = INVISIBLE
                    }
                    1 ->
                    {
                        constraint_start.visibility = INVISIBLE
                        constraint_end.visibility = VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: Tab?)
            {
            }

            override fun onTabReselected(tab: Tab?)
            {
            }

        })

        layoutView.datePicker_start.hour = startTimeList[0].toInt()
        layoutView.datePicker_start.minute = startTimeList[1].toInt()

        layoutView.datePicker_end.hour = endTimeList[0].toInt()
        layoutView.datePicker_end.minute = endTimeList[1].toInt()

        setClickEvent()

        return layoutView
    }

    private fun setClickEvent(){

        layoutView.tv_cancelBtn.setOnClickListener {
            SopoLog.d(tag = TAG, msg = "Cancel button")
            dismiss()
        }

        layoutView.tv_okBtn.setOnClickListener {
            SopoLog.d(tag = TAG, msg = "Ok button")

            val startHour = layoutView.datePicker_start.hour
            val startMin = layoutView.datePicker_start.minute
            val endHour = layoutView.datePicker_end.hour
            val endMin = layoutView.datePicker_end.minute

            val toStartTotalMin = startHour * 60 + startMin
            val toEndTotalMin = endHour * 60 + endMin

            val abs = abs(toStartTotalMin - toEndTotalMin)

            var startTime = "00:00"
            var endTime = "00:00"

            if(abs <= 90)
            {
                Toast.makeText(SOPOApp.INSTANCE, "방해 금지 시간대는 최소 한시간 반이상이어야 합니다.", Toast.LENGTH_LONG).show()
                startTime = "00:00"
                endTime = "00:00"
            }
            else
            {
                startTime = "${startHour.toString().padStart(2, '0')}:${startMin.toString().padStart(2, '0')}"
                endTime = "${endHour.toString().padStart(2, '0')}:${endMin.toString().padStart(2, '0')}"
            }

            callback.invoke(startTime, endTime)
            dismiss()
        }
    }

    private fun setSetting() {
        isCancelable = true
        dialog?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }
    }
}
