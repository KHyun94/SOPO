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
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.delivery.sopo.R
import com.delivery.sopo.databinding.ConfirmDeleteDialogBinding
import com.delivery.sopo.enums.OptionalTypeEnum

typealias OptionalClickListener = (dialog: OptionalDialog)-> Unit

class OptionalDialog : DialogFragment
{

    lateinit var binding: ConfirmDeleteDialogBinding

    private val optionalType: OptionalTypeEnum
    @DrawableRes private var titleIcon: Int = 0
    private val title: String
    private val subTitle: String?
    private val content: String
    private val leftHandler: Pair<String,OptionalClickListener>
    private val rightHandler: Pair<String,OptionalClickListener>

    constructor(optionalType: OptionalTypeEnum = OptionalTypeEnum.LEFT, @DrawableRes titleIcon: Int, title: String, subTitle: String? = null, content: String, leftHandler: Pair<String,OptionalClickListener>, rightHandler: Pair<String,OptionalClickListener>): super() {
        this.optionalType = optionalType
        this.titleIcon = titleIcon
        this.title = title
        this.subTitle = subTitle
        this.content = content
        this.leftHandler = leftHandler
        this.rightHandler = rightHandler
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ConfirmDeleteDialogBinding.inflate(LayoutInflater.from(context))
        binding.lifecycleOwner = activity
        binding.optionalType = optionalType
        setWindowSetting()

        binding.titleIcon = titleIcon
        binding.title = title
        binding.subTitle = subTitle
        binding.content = content

        binding.leftBtnText = leftHandler.first
        binding.leftBtnClickListener = View.OnClickListener { leftHandler.second.invoke(this) }

        binding.rightBtnText = rightHandler.first
        binding.rightBtnClickListener = View.OnClickListener { rightHandler.second.invoke(this) }

        /*setClickEvent()
        setUI()*/
        return binding.root
    }

    private fun setClickEvent(){
        binding.leftBtnText = leftHandler.first
        binding.leftBtnClickListener = View.OnClickListener { leftHandler.second.invoke(this) }

        binding.rightBtnText = rightHandler.first
        binding.rightBtnClickListener = View.OnClickListener { rightHandler.second.invoke(this) }
    }


    private fun setWindowSetting() {
        isCancelable = true
        dialog?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }
    }

/*    private fun setMainButtonUI(){
        binding.tvCancel.run {
            setBackgroundResource(R.drawable.border_all_rounded_main_blue)
            setTextColor(resources.getColor(R.color.MAIN_WHITE))
        }

        binding.tvDelete.run {
            setBackgroundResource(R.drawable.border_15dp_blue_rounder)
            setTextColor(resources.getColor(R.color.COLOR_MAIN_500))
        }
    }*/

    private fun setUI()
    {
    /*    setTitleIcon(titleIcon)
        setTitle(title)
        setSubTitle(subTitle)
        setContent(content)*/

/*        if(title != null)
        {
            binding.tvCancel.run {
                setBackgroundResource(R.drawable.border_all_rounded_main_blue)
                setTextColor(resources.getColor(R.color.MAIN_WHITE))
            }

            binding.tvDelete.run {
                setBackgroundResource(R.drawable.border_15dp_blue_rounder)
                setTextColor(resources.getColor(R.color.COLOR_MAIN_500))
            }
        }*/
    }

 /*   private fun setTitleIcon(@DrawableRes icon: Int)
    {
        Glide.with(this)
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
    }*/
}
