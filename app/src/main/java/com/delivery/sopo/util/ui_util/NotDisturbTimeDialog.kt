package com.delivery.sopo.util.ui_util

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.delivery.sopo.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.*
import kotlinx.android.synthetic.main.permission_dialog.view.*
import kotlinx.android.synthetic.main.set_not_disturb_time_dialog.*
import kotlinx.android.synthetic.main.set_not_disturb_time_dialog.view.*


class NotDisturbTimeDialog : DialogFragment {

    private var parentActivity: Activity

    private var title: String? = null

    private var onOkClickListener: ((agree: NotDisturbTimeDialog) -> Unit)? = null

    private lateinit var layoutView: View

    private lateinit var permissionLayout : LinearLayout

    constructor(
        act: Activity,
        handler: ((agree: NotDisturbTimeDialog) -> Unit)
    ) : super() {
        this.parentActivity = act
        this.onOkClickListener = handler
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

        return layoutView
    }

    private fun setClickEvent() {

        layoutView.btn_ok.setOnClickListener {
            if (onOkClickListener == null) {
                dismiss()
            } else {
                onOkClickListener?.invoke(this)
            }
        }

    }

    private fun setSetting() {
        isCancelable = false
        dialog?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }
    }
}
