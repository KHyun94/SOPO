package com.delivery.sopo.bindings

import android.widget.FrameLayout
import androidx.databinding.BindingAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

object TabBindingAdapter
{
    @JvmStatic
    @BindingAdapter("setTabSelectedListener")
    fun bindTabLayoutSelectListener(view:TabLayout, listener: OnTabSelectedListener){
        view.addOnTabSelectedListener(listener)
    }


}