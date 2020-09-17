package com.delivery.sopo.bindings

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object ImageBindingAdapter
{
    @JvmStatic
    @BindingAdapter("setImage")
    fun bindSetterImage(view: ImageView, res:Int){
        Glide.with(view.context)
            .load(res)
            .into(view)
    }

    @JvmStatic
    @BindingAdapter("isClick", "trueRes", "falseRes")
    fun bindSetterSelectedImage(view: ImageView, isClick : Boolean, trueRes:Int, falseRes:Int){

        val selectRes = if(isClick) trueRes else falseRes

        Glide.with(view.context)
            .load(selectRes)
            .into(view)
    }

    @JvmStatic
    @BindingAdapter("setImage")
    fun bindSetterGif(view: ImageView, res:Int){
        Glide.with(view.context)
            .asGif()
            .load(res)
            .into(view)
    }
}