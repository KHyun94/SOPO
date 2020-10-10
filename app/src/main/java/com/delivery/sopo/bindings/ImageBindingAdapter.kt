package com.delivery.sopo.bindings

import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object ImageBindingAdapter
{
    @JvmStatic
    @BindingAdapter("setImage")
    fun bindSetterImage(view: ImageView, res: Int)
    {
        Glide.with(view.context)
            .load(res)
            .into(view)
    }

    @JvmStatic
    @BindingAdapter("isClick", "trueRes", "falseRes")
    fun bindSetterSelectedImage(view: ImageView, isClick: Boolean, trueRes: Int, falseRes: Int)
    {

        val selectRes = if (isClick) trueRes else falseRes

        Glide.with(view.context)
            .load(selectRes)
            .into(view)
    }

    @JvmStatic
    @BindingAdapter("setImageAsGif")
    fun bindSetterGif(view: ImageView, res: Int)
    {
        Log.d("LOG.SOPO", "왓 $res")
        Glide.with(view.context)
            .asGif()
            .load(res)
            .into(view)
    }

    // todo 0928 kh indicator 용 ... 나중에 전체적으로 호환가능하게 처리
    @JvmStatic
    @BindingAdapter("isSelect", "selectRes", "unselectRes")
    fun bindSelectImage(view: ImageView, isSelect: Boolean, selectRes: Int, unselectRes: Int)
    {
        if (isSelect)
        {
            Glide.with(view.context)
                .asGif()
                .load(selectRes)
                .into(view)
        }
        else
        {
            Glide.with(view.context)
                .load(unselectRes)
                .into(view)
        }


    }
}