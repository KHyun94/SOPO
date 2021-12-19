package com.delivery.sopo.util

import android.view.View
import android.view.animation.TranslateAnimation

object AnimationUtil
{
    fun slideUp(view: View)
    {
        view.visibility = View.VISIBLE
        val animate = TranslateAnimation(0F,  // fromXDelta
                                         0F,  // toXDelta
                                         view.height.toFloat(),  // fromYDelta
                                         0F) // toYDelta
        animate.duration = 300
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    fun slideDown(view: View)
    {
        view.visibility = View.GONE
        val animate = TranslateAnimation(0F,  // fromXDelta
                                         0F,  // toXDelta
                                         0F,  // fromYDelta
                                         view.height.toFloat()) // toYDelta
        animate.duration = 300
        animate.fillAfter = true
        view.startAnimation(animate)
    }
}