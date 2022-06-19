package com.delivery.sopo.presentation.bindings

import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.Placeholder
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.delivery.sopo.R
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.PersonalMessageEnum
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog

object ImageBindingAdapter
{
    @JvmStatic
    @BindingAdapter("setLottieRawRes")
    fun bindLottieSetter(view: LottieAnimationView, enum: PersonalMessageEnum?)
    {
        SopoLog.d("Test Lottie ${enum.toString()}")
        when(enum)
        {
            PersonalMessageEnum.NEW ->
            {
                view.setAnimation(R.raw.lottie_personal_message_welcome)
            }
            PersonalMessageEnum.HELLO ->
            {
                view.setAnimation(R.raw.lottie_personal_message_hello)
            }
            PersonalMessageEnum.SHOPPING_DETOX ->
            {
                view.setAnimation(R.raw.lottie_personal_message_shopping_detox)
            }
            PersonalMessageEnum.PRO_UNBOXING ->
            {
                view.setAnimation(R.raw.lottie_personal_message_pro_unboxing)
            }
            PersonalMessageEnum.SOPO_CNT ->
            {
                view.setAnimation(R.raw.lottie_personal_message_sopo_cnt)
            }
            PersonalMessageEnum.WAITING ->
            {
                view.setAnimation(R.raw.lottie_personal_message_waiting)
            }
            PersonalMessageEnum.BE_QUIET ->
            {
                view.setAnimation(R.raw.lottie_personal_message_be_quiet)
            }
            else -> 0
        }
        view.playAnimation()
    }

    @JvmStatic
    @BindingAdapter("setDeliveryStatusInLottie")
    fun bindLottieBackgroundSetter(view: LottieAnimationView, enum: DeliveryStatusEnum?)
    {
        when(enum)
        {
            DeliveryStatusEnum.NOT_REGISTERED ->
            {
                view.background =
                    ContextCompat.getDrawable(view.context, R.drawable.ic_inquiry_2depth_not_registered)
            }
            DeliveryStatusEnum.INFORMATION_RECEIVED ->
            {
                view.background =
                    ContextCompat.getDrawable(view.context, R.drawable.ic_inquiry_2depth_not_registered)

            }
            DeliveryStatusEnum.AT_PICKUP ->
            {
                view.setAnimation(R.raw.inquiry_2depth_at_pickup)

            }
            DeliveryStatusEnum.IN_TRANSIT ->
            {
                view.setAnimation(R.raw.inquiry_2depth_in_transit)

            }
            DeliveryStatusEnum.OUT_FOR_DELIVERY ->
            {
                view.setAnimation(R.raw.inquiry_2depth_out_for_delivery)
            }
            DeliveryStatusEnum.DELIVERED ->
            {
                view.setAnimation(R.raw.inquiry_2depth_delivered)

            }
            DeliveryStatusEnum.ERROR ->
            {
            }
            else ->
            {
            }
        }
        view.playAnimation()
    }


    @JvmStatic
    @BindingAdapter("setBackgroundColor")
    fun bindBackgroundColor(view: View, @ColorRes colorRes: Int)
    {
        try
        {
            view.setBackgroundResource(colorRes)
        }
        catch(e: java.lang.Exception)
        {
            SopoLog.e(e.localizedMessage, e)
            view.setBackgroundResource(0)
        }

    }

    @JvmStatic
    @BindingAdapter("setBackground")
    fun bindImageViewIcon(view: ImageView, res: Int)
    {
        try
        {
            when(OtherUtil.getResourceExtension(res))
            {
                "gif" ->
                {
                    Glide.with(view)
                        .asGif()
                        .load(res)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(view)
                }
                else ->
                {
                    Glide.with(view)
                        .load(res)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(view)
                }
            }
        }
        catch(e: Exception)
        {
            Glide.with(view.context).load(res).into(view)
        }
    }

    @JvmStatic
    @BindingAdapter("setImage", "setDefaultImage")
    fun bindSetterImage(view: ImageView, res: Int, defaultRes: Int = 0)
    {
        try
        {
            val mimeTypedValue = OtherUtil.getResourceExtension(res)

            when(mimeTypedValue)
            {
                "gif" ->
                {
                    Glide.with(view.context)
                        .asGif()
                        .load(res)
                        .placeholder(defaultRes)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(view)
                }
                else ->
                {
                    Glide.with(view.context)
                        .load(res)
                        .placeholder(0)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(view)
                }
            }
        }
        catch(e: Exception)
        {
            Glide.with(view.context).load(res).into(view)
        }
    }

    @JvmStatic
    @BindingAdapter("isClick", "trueRes", "falseRes")
    fun bindSetterSelectedImage(view: ImageView, isClick: Boolean, trueRes: Int, falseRes: Int)
    {
        val selectRes = if(isClick) trueRes else falseRes

        Glide.with(view.context).load(selectRes).into(view)
    }

    @JvmStatic
    @BindingAdapter("setImageAsGif")
    fun bindSetterGif(view: ImageView, res: Int)
    {
        Log.d("LOG.SOPO", "왓 $res")
        Glide.with(view.context).asGif().load(res).into(view)
    }

    // todo 0928 kh indicator 용 ... 나중에 전체적으로 호환가능하게 처리
    @JvmStatic
    @BindingAdapter("isSelect", "selectRes", "unselectRes")
    fun bindSelectImage(view: ImageView, isSelect: Boolean, selectRes: Int, unselectRes: Int)
    {
        if(isSelect)
        {
            Glide.with(view.context).asGif().load(selectRes).into(view)
        }
        else
        {
            Glide.with(view.context).load(unselectRes).into(view)
        }


    }
}