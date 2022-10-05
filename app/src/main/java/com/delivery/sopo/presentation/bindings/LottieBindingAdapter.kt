package com.delivery.sopo.presentation.bindings

import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.airbnb.lottie.LottieAnimationView
import com.delivery.sopo.R
import com.delivery.sopo.enums.DeliveryStatus
import com.delivery.sopo.enums.ParcelDepth

object LottieBindingAdapter {
    @JvmStatic
    @BindingAdapter("setLottieBackground")
    fun bindLottieBackgroundSetter(view: LottieAnimationView, deliveryStatus: DeliveryStatus)
    {
        val icon = ParcelDepth.getParcelFirstDepth(deliveryStatus = deliveryStatus.name).iconRes

        when(deliveryStatus)
        {
            DeliveryStatus.NOT_REGISTERED ->
            {
                view.background = ContextCompat.getDrawable(view.context, icon)
            }
            DeliveryStatus.INFORMATION_RECEIVED ->
            {
                view.background = ContextCompat.getDrawable(view.context, icon)

            }
            DeliveryStatus.AT_PICKUP ->
            {
                view.background = ContextCompat.getDrawable(view.context, icon)

            }
            DeliveryStatus.IN_TRANSIT ->
            {
                view.background = ContextCompat.getDrawable(view.context, icon)
            }
            DeliveryStatus.OUT_FOR_DELIVERY ->
            {
                view.background = ContextCompat.getDrawable(view.context, icon)
            }
            DeliveryStatus.DELIVERED ->
            {
                view.background = ContextCompat.getDrawable(view.context, icon)
            }
            DeliveryStatus.ERROR ->
            {
                view.background = ContextCompat.getDrawable(view.context, icon)
            }
            else ->
            {
            }
        }
        view.playAnimation()
    }

}