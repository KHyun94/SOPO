package com.delivery.sopo.views.dialog

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.delivery.sopo.R
import com.delivery.sopo.util.SopoLog
import com.google.android.material.tabs.TabLayout.*
import kotlinx.android.synthetic.main.set_not_disturb_time_dialog.*
import kotlinx.android.synthetic.main.set_not_disturb_time_dialog.view.*


class NotDisturbTimeDialog : DialogFragment {

    private var parentActivity: Activity
    private lateinit var layoutView: View
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"

    constructor(act: Activity) : super() {
        this.parentActivity = act
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

        tabs.addOnTabSelectedListener(object: OnTabSelectedListener{
            override fun onTabSelected(tab: Tab?)
            {
                when(tabs.selectedTabPosition){
                    0 -> {
                        constraint_start.visibility = VISIBLE
                        constraint_end.visibility = INVISIBLE
                    }
                    1 -> {
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

        setClickEvent()

        return layoutView
    }

    private fun setClickEvent(){

        layoutView.tv_cancelBtn.setOnClickListener {
            SopoLog.d(tag = TAG, msg = "Ok button")
            dismiss()
        }

        layoutView.tv_okBtn.setOnClickListener {
            SopoLog.d(tag = TAG, msg = "Ok button")
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
