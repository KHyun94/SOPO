package com.delivery.sopo.bindings

import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.delivery.sopo.util.OtherUtil

object ImageBindingAdapter
{
    @JvmStatic
    @BindingAdapter("setImage", "setDefaultImage")
    fun bindSetterImage(view: ImageView, res: Int, defaultRes :Int = 0)
    {
        try
        {
            val mimeTypedValue = OtherUtil.getResourceExtension(res)

            when (mimeTypedValue)
            {
                "gif" ->
                {
                    Glide.with(view.context)
                        .asGif()
                        .load(res)
                        .placeholder(defaultRes)
                        .into(view)
                }
                else ->
                {
                    Glide.with(view.context)
                        .load(res)
                        .placeholder(0)
                        .into(view)
                }
            }
        }
        catch (e: Exception)
        {
            Glide.with(view.context)
                .load(res)
                .into(view)
        }


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