package com.delivery.sopo.bindings

import android.view.View
import androidx.databinding.BindingAdapter

object OtherBindingAdapter
{
    @JvmStatic
    @BindingAdapter("onTouchListener")
    fun bindOnTouchListener(
        v:View,
        listener:View.OnTouchListener
    ){
        v.isFocusableInTouchMode = true
        v.setOnTouchListener(listener)
    }

    @JvmStatic
    @BindingAdapter("clearFocus")
    fun bindClearFocus(
        v:View,
        isClean:Boolean
    ){
        if(isClean)
            v.clearFocus()
    }
}