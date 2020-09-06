package com.delivery.sopo.util.ui_util

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.DialogFragment
import com.delivery.sopo.R
import com.google.android.material.tabs.TabLayout.*
import kotlinx.android.synthetic.main.other_faq_dialog.view.*
import kotlinx.android.synthetic.main.set_not_disturb_time_dialog.*
import kotlinx.android.synthetic.main.set_not_disturb_time_dialog.view.*


class OtherFaqDialog : DialogFragment {

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
        layoutView = inflater.inflate(R.layout.other_faq_dialog, container, false)
        setSetting()
        setClickEvent()

        return layoutView
    }

    private fun setClickEvent(){

        layoutView.tv_close.setOnClickListener {
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
