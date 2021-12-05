package com.delivery.sopo.views.dialog

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.DrawableRes
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.delivery.sopo.R
import com.delivery.sopo.databinding.ConfirmDeleteDialogBinding

class ConfirmDeleteDialog : DialogFragment {

    lateinit var binding: ConfirmDeleteDialogBinding

    private var parentActivity: Activity


    @DrawableRes private var titleIcon: Int = 0
    private var title: String? = null
    private var subTitle: String? = null
    private var content: String? = null
    private var handler: Pair<String,(agree: ConfirmDeleteDialog)-> Unit>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ConfirmDeleteDialogBinding.inflate(LayoutInflater.from(context))
        setSetting()
        setClickEvent()
        setUI()
        return binding.root
    }

    private fun setClickEvent(){

        binding.tvDelete.text = handler.first
        binding.tvDelete.setOnClickListener {
            handler.second.invoke(this)
        }

        binding.tvCancel.setOnClickListener {
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

    private fun setUI()
    {
        setTitleIcon(titleIcon)
        setTitle(title)
        setSubTitle(subTitle)
        setContent(content)

        if(title != null)
        {
            binding.tvCancel.run {
                setBackgroundResource(R.drawable.border_all_rounded_main_blue)
                setTextColor(resources.getColor(R.color.MAIN_WHITE))
            }

            binding.tvDelete.run {
                setBackgroundResource(R.drawable.border_15dp_blue_rounder)
                setTextColor(resources.getColor(R.color.COLOR_MAIN_500))
            }
        }
    }

    private fun setTitleIcon(@DrawableRes icon: Int)
    {
        Glide.with(parentActivity)
            .load(icon)
            .into(binding.ivTitleIcon)

        binding.ivTitleIcon.visibility = View.VISIBLE
    }

    private fun setTitle(title: String?)
    {
        if(title == null) return
        binding.tvDialogTitle.text = title
    }

    private fun setSubTitle(subTitle: String?)
    {
        if(subTitle == null) return
        binding.tvSubTitle.text = subTitle
        binding.layoutSubTitle.visibility = View.VISIBLE
    }

    private fun setContent(content: String?)
    {
        if(content == null) return
        binding.tvContent.text = content
    }
}
