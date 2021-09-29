package com.delivery.sopo.interfaces.listener

import android.view.View

interface OnParcelClickListener
{
    fun onParcelClicked(view: View, type : Int, parcelId:Int)
    fun onParcelLongClicked(view: View, type : Int, parcelId:Int)
}