package com.delivery.sopo.interfaces.listener

import android.view.View
import com.delivery.sopo.models.parcel.ParcelId

interface OnParcelClickListener
{
    fun onItemClicked(view: View, type : Int, parcelId: ParcelId)
    fun onItemLongClicked(view: View, type : Int, parcelId: ParcelId)
}