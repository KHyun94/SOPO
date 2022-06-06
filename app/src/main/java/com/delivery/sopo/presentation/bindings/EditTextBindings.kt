package com.delivery.sopo.presentation.bindings

import android.view.View
import android.widget.EditText
import androidx.databinding.BindingAdapter
import com.delivery.sopo.enums.InfoEnum
import com.google.android.material.textfield.TextInputLayout

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