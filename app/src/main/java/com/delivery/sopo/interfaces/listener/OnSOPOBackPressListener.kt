package com.delivery.sopo.interfaces.listener

import android.view.View
import com.delivery.sopo.enums.InquiryStatusEnum

interface OnSOPOBackPressListener
{
    fun onBackPressedInTime()
    fun onBackPressedOutTime()
    fun onBackPressed()
}

open class OnSOPOBackPressEvent(private val isUseCommon:Boolean = false): OnSOPOBackPressListener
{
    open override fun onBackPressedInTime()
    {
        if(isUseCommon) return
    }

    open override fun onBackPressedOutTime()
    {
        if(isUseCommon) return
    }

    open override fun onBackPressed()
    {
        if(!isUseCommon) return
    }

}