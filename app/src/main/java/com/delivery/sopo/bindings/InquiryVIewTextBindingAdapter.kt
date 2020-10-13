package com.delivery.sopo.bindings

import android.graphics.Color
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.delivery.sopo.enums.ScreenStatusEnum

object InquiryVIewTextBindingAdapter
{
    @JvmStatic
    @BindingAdapter("setOngoingView")
    fun bindingOngoingTextViewAdapter(
        tv: TextView,
        screenStatusEnum: ScreenStatusEnum
    ){
        when(screenStatusEnum){
            ScreenStatusEnum.ONGOING ->{

            }
            ScreenStatusEnum.COMPLETE ->{
                tv.text = "배송완료"
                tv.setTextColor(Color.WHITE)
            }
        }
    }
}