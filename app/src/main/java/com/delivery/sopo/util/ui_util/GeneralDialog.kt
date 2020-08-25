package com.delivery.sopo.util.ui_util

import android.app.ActionBar
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.delivery.sopo.R
import com.delivery.sopo.util.fun_util.SizeUtil
import kotlinx.android.synthetic.main.general_dialog.view.*

typealias OnAgreeClickListener = (agree: GeneralDialog) -> Unit

class GeneralDialog : DialogFragment {

    private var parentActivity: Activity

    private var title: String? = null
    private var msg: String? = null
    private var detailMsg: String? = null

    private var onRightClickListener: Pair<String, OnAgreeClickListener?>? = null
    private var onLeftClickListener: Pair<String, OnAgreeClickListener?>? = null

    private lateinit var layoutView: View

    constructor(
        act: Activity,
        title: String,
        msg: String,
        detailMsg: String?,
        rHandler: Pair<String, OnAgreeClickListener?>
    ) : super() {
        this.parentActivity = act
        this.title = title
        this.msg = msg
        this.detailMsg = detailMsg
        this.onRightClickListener = rHandler
    }

    constructor(
        act: Activity,
        title: String,
        msg: String,
        detailMsg: String?,
        rHandler: Pair<String, OnAgreeClickListener?>,
        lHandler: Pair<String, OnAgreeClickListener?>
    ) : super() {
        this.parentActivity = act
        this.title = title
        this.msg = msg
        this.detailMsg = detailMsg
        this.onRightClickListener = rHandler
        this.onLeftClickListener = lHandler
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layoutView = inflater.inflate(R.layout.general_dialog, container, false)

        setSetting()
        setUI()
        setClickEvent()

        return layoutView
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            (SizeUtil.getDeviceSize(parentActivity).x * 4 / 5),
            ActionBar.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setUI() {
        layoutView.tv_title.text = title
        layoutView.tv_simple_msg.text = msg

        if (detailMsg != null) {
            layoutView.tv_detail_msg.text = detailMsg
        } else {
            layoutView.tv_expand_layout.visibility = View.GONE
            layoutView.iv_arrow.visibility = View.GONE
        }

        if (onRightClickListener != null)
            layoutView.btn_right.text = onRightClickListener?.first
        else
            layoutView.btn_right.text = "네"

        if (onLeftClickListener != null)
            layoutView.btn_left.text = onLeftClickListener?.first
        else {
//            layoutView.btn_left.text = "아니오"
            layoutView.btn_left.visibility = View.GONE
        }

    }

    private fun setClickEvent() {

        // 자세히 보기
        layoutView.tv_expand_layout.setOnClickListener {

            if (layoutView.layout_detail.visibility == View.VISIBLE) {
                layoutView.layout_detail.visibility = View.GONE
                layoutView.iv_arrow.setBackgroundResource(R.drawable.down_arrow)
            } else {
                layoutView.layout_detail.visibility = View.VISIBLE
                layoutView.iv_arrow.setBackgroundResource(R.drawable.up_arrow)
            }
        }

        layoutView.btn_right.setOnClickListener {
            if (onRightClickListener?.second == null) {
                dismiss()
            } else {
                onRightClickListener?.second?.invoke(this)
            }
        }

        layoutView.btn_left.setOnClickListener {
            if (onLeftClickListener?.second == null) {
                dismiss()
            } else {
                onLeftClickListener?.second?.invoke(this)
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
