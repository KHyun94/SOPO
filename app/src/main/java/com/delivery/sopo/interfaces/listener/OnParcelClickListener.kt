package com.delivery.sopo.interfaces.listener

import android.view.View

interface OnParcelClickListener
{
    fun onItemClicked(view: View, type : Int, parcelId:Int)
    fun onItemLongClicked(view: View, type : Int, parcelId:Int)
}