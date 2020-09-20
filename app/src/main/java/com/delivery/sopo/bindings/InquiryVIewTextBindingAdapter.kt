package com.delivery.sopo.bindings

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.delivery.sopo.R
import com.delivery.sopo.enums.ScreenStatus

object InquiryVIewTextBindingAdapter
{
    @JvmStatic
    @BindingAdapter("setOngoingView")
    fun bindingOngoingTextViewAdapter(
        tv: TextView,
        screenStatus: ScreenStatus
    ){
        when(screenStatus){
            ScreenStatus.ONGOING ->{

            }
            ScreenStatus.COMPLETE ->{
                tv.text = "배송완료"
                tv.setTextColor(Color.WHITE)
            }
        }
    }
}