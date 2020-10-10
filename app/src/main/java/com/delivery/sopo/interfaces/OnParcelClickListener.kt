package com.delivery.sopo.interfaces

import android.view.View
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.parcel.ParcelId

interface OnParcelClickListener
{
    fun onItemClicked(view : View, parcelId : ParcelId)
//    fun onOnGoingClicked(v: View, parcelId: ParcelId)
//    fun onCompleteClicked()
}