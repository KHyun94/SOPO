package com.delivery.sopo.bindings

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.delivery.sopo.R

object TextViewBindingAdapter
{
    @JvmStatic
    @BindingAdapter("setText", "setTextColor")
    fun bindPropertyByTextView(tv: TextView, content: String,@ColorRes colorRes: Int = R.color.MAIN_BLACK)
    {
        tv.run {
            text = content
            setTextColor(ContextCompat.getColor(context, colorRes))
        }
    }

    @JvmStatic
    @BindingAdapter("focusChangeListener")
    fun bindOnFocusChangeListener(tv: TextView, onFocusChangeListener: View.OnFocusChangeListener)
    {
        tv.onFocusChangeListener = onFocusChangeListener
    }

}