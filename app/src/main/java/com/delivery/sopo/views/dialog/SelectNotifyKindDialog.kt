package com.delivery.sopo.views.dialog

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.delivery.sopo.R
import kotlinx.android.synthetic.main.select_notify_kind_dialog.view.*


class SelectNotifyKindDialog : DialogFragment {

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
        layoutView = inflater.inflate(R.layout.select_notify_kind_dialog, container, false)
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
