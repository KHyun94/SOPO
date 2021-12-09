package com.delivery.sopo.bindings

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.delivery.sopo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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