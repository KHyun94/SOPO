package com.delivery.sopo.bindings

import android.os.Handler
import android.view.View
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.util.SopoLog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_not_disturb_time.view.*

typealias FocusChangeCallback = (View, Boolean, InfoEnum) -> Unit

object EditTextBindings
{
    @JvmStatic
    @BindingAdapter("type","focusChangeListener")
    fun bindFocusChangeListener(
        et: EditText,
        type: InfoEnum,
        focusChangeCallback: FocusChangeCallback
    )
    {
        et.setOnFocusChangeListener{v, hasFocus ->
            focusChangeCallback.invoke(v, hasFocus, type)
        }
    }

    @JvmStatic
    @BindingAdapter("errorMessage")
    fun bindErrorMessage(
        layout: TextInputLayout,
        errorMessage: String?
    )
    {
        layout.error = errorMessage
    }
}