package com.delivery.sopo.views.dialog

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.delivery.sopo.R
import kotlinx.android.synthetic.main.confirm_delete_dialog.view.*


class ConfirmDeleteDialog : DialogFragment {

    private var parentActivity: Activity
    private lateinit var layoutView: View
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"
    private var onOkClickListener: ((agree: ConfirmDeleteDialog) -> Unit)? = null

    constructor(act: Activity, handler: (agree: ConfirmDeleteDialog) -> Unit) : super() {
        this.parentActivity = act
        onOkClickListener = handler
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layoutView = inflater.inflate(R.layout.confirm_delete_dialog, container, false)
        setSetting()
        setClickEvent()

        return layoutView
    }

    private fun setClickEvent(){

        layoutView.tv_delete.setOnClickListener {

            onOkClickListener?.invoke(this)
        }

        layoutView.tv_cancel.setOnClickListener {
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
