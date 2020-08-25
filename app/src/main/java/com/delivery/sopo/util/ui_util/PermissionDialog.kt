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
import kotlinx.android.synthetic.main.permission_dialog.view.*

typealias OnClickListener = (agree: PermissionDialog) -> Unit

class PermissionDialog : DialogFragment {

    private var parentActivity: Activity

    private var title: String? = null

    private var onOkClickListener: OnClickListener? = null

    private lateinit var layoutView: View

    private lateinit var permissionLayout : LinearLayout

    constructor(
        act: Activity,
        handler: OnClickListener
    ) : super() {
        this.parentActivity = act
        this.onOkClickListener = handler
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layoutView = inflater.inflate(R.layout.permission_dialog, container, false)

        setSetting()
        setClickEvent()

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
