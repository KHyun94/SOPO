package com.delivery.sopo.bindings

import android.widget.FrameLayout
import androidx.databinding.BindingAdapter
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

object TabBindingAdapter
{
    @JvmStatic
    @BindingAdapter("setTabSelectedListener")
    fun bindTabLayoutSelectListener(view:TabLayout, listener: OnTabSelectedListener){
        view.addOnTabSelectedListener(listener)
        view.getTabAt(0)!!.icon!!.setTint(SOPOApp.INSTANCE.resources.getColor(R.color.COLOR_MAIN_BLUE))
    }


}