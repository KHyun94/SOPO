package com.delivery.sopo.interfaces.listener

import android.view.View
import com.delivery.sopo.enums.InquiryStatusEnum

interface OnParcelClickListener
{
    fun onEnterParcelDetailClicked(view: View, type : InquiryStatusEnum, parcelId:Int)
    fun onMaintainParcelClicked(view: View, type : InquiryStatusEnum, parcelId:Int)
    fun onUpdateParcelAliasClicked(view: View, type : InquiryStatusEnum, parcelId:Int)
    fun onParcelDeleteClicked(view:View, type : InquiryStatusEnum, parcelId:Int)
}

open class ParcelEventListener: OnParcelClickListener
{
    override fun onEnterParcelDetailClicked(view: View, type : InquiryStatusEnum, parcelId: Int) {}

    override fun onMaintainParcelClicked(view: View, type : InquiryStatusEnum, parcelId:Int) {}

    override fun onUpdateParcelAliasClicked(view: View, type : InquiryStatusEnum, parcelId: Int) {}

    override fun onParcelDeleteClicked(view:View, type : InquiryStatusEnum, parcelId:Int) {}
}