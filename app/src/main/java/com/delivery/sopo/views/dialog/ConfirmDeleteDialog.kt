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

typealias OnDeleteClickListener = Pair<String,(agree: ConfirmDeleteDialog)-> Unit>

class ConfirmDeleteDialog : DialogFragment {

    private var parentActivity: Activity
    private lateinit var layoutView: View

    @DrawableRes private var titleIcon: Int = 0
    private var title: String? = null
    private var subTitle: String? = null
    private var content: String? = null
    private var handler: Pair<String,(agree: ConfirmDeleteDialog)-> Unit>

    constructor(act: Activity, handler: Pair<String,(agree: ConfirmDeleteDialog)-> Unit>) : super() {
        this.parentActivity = act
        this.handler = handler
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layoutView = inflater.inflate(R.layout.confirm_delete_dialog, container, false)
        setSetting()
        setClickEvent()
        setUI()
        return layoutView
    }

    private fun setClickEvent(){

        layoutView.tv_delete.text = handler.first
        layoutView.tv_delete.setOnClickListener {
            handler.second.invoke(this)
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

    constructor(act: Activity, @DrawableRes titleIcon: Int, title: String, subTitle: String, content: String, handler: Pair<String,(agree: ConfirmDeleteDialog)-> Unit>) : super() {
        this.parentActivity = act
        this.titleIcon = titleIcon
        this.title = title
        this.subTitle = subTitle
        this.content = content
        this.handler = handler
    }

    fun setUI()
    {
        setTitleIcon(titleIcon)
        setTitle(title)
        setSubTitle(subTitle)
        setContent(content)
    }

    fun setTitleIcon(@DrawableRes icon: Int)
    {
        Glide.with(parentActivity)
            .load(icon)
            .into(layoutView.iv_title_icon)

        layoutView.iv_title_icon.visibility = View.VISIBLE
    }

    fun setTitle(title: String?)
    {
        if(title == null) return
        layoutView.tv_dialog_title.text = title
    }

    fun setSubTitle(subTitle: String?)
    {
        if(subTitle == null) return
        layoutView.tv_sub_title.text = subTitle
        layoutView.layout_sub_title.visibility = View.VISIBLE
    }

    fun setContent(content: String?)
    {
        if(content == null) return
        layoutView.tv_content.text = content
    }
}
