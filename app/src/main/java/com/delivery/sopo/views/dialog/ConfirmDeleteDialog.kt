package com.delivery.sopo.views.dialog

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.annotation.DrawableRes
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.delivery.sopo.R
import kotlinx.android.synthetic.main.confirm_delete_dialog.*
import kotlinx.android.synthetic.main.confirm_delete_dialog.view.*


class ConfirmDeleteDialog : DialogFragment {

    private var parentActivity: Activity
    private lateinit var layoutView: View
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

    fun setTitleIcon(@DrawableRes icon: Int)
    {
        Glide.with(parentActivity)
            .load(icon)
            .into(layoutView.iv_title_icon)

        layoutView.iv_title_icon.visibility = View.VISIBLE
    }

    fun setTitle(title: String)
    {
        layoutView.tv_dialog_title.text = title
    }

    fun setSubTitle(subTitle: String)
    {
        layoutView.tv_sub_title.text = subTitle
        layoutView.layout_sub_title.visibility = View.VISIBLE
    }

    fun setDeleteClick(text: String)
    {
        layoutView.tv_delete.text = text
    }

    fun setContent(text: String)
    {
        layoutView.tv_content.text = text
    }
}
