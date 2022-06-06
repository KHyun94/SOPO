package com.delivery.sopo.presentation.bindings

import android.graphics.Color
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.delivery.sopo.enums.InquiryStatusEnum

object InquiryVIewTextBindingAdapter
{
    @JvmStatic
    @BindingAdapter("setOngoingView")
    fun bindingOngoingTextViewAdapter(
            tv: TextView,
            inquiryStatusEnum: InquiryStatusEnum
    ){
        when(inquiryStatusEnum){
            InquiryStatusEnum.ONGOING ->{

            }
            InquiryStatusEnum.COMPLETE ->{
                tv.text = "배송완료"
                tv.setTextColor(Color.WHITE)
            }
        }
    }
}