package com.delivery.sopo.bindings

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.tabs.TabLayout

object ImageBindingAdapter
{
    @JvmStatic
    @BindingAdapter("setImage")
    fun bindTabLayoutSelectListener(view: ImageView, res:Int){
        view.setBackgroundResource(res)
    }
}