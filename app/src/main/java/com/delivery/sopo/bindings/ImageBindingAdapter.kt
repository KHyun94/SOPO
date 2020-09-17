package com.delivery.sopo.bindings

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object ImageBindingAdapter
{
    @JvmStatic
    @BindingAdapter("setImage")
    fun bindTabLayoutSelectListener(view: ImageView, res:Int){
        Glide.with(view.context)
            .load(res)
            .into(view)
    }
}