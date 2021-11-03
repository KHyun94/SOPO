package com.delivery.sopo.bindings

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseMethod
import com.delivery.sopo.util.SopoLog

object ViewBindingAdapter
{
    @JvmStatic
    @BindingAdapter("android:visibility")
    fun setVisibility(v: View, value: Int){

        v.visibility = when (value)
        {
            View.VISIBLE -> View.VISIBLE
            View.INVISIBLE -> View.INVISIBLE
            View.GONE -> View.GONE
            else -> View.GONE
        }
    }
}