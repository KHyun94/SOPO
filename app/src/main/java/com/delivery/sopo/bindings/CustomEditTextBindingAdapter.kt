package com.delivery.sopo.bindings

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.delivery.sopo.util.ui_util.CustomEditText
import kotlinx.android.synthetic.main.custom_edit_text.view.*

object CustomEditTextBindingAdapter
{

    // 커스텀 뷰 two-binding
    @JvmStatic
    @BindingAdapter("content")
    fun setCustomEtView(v: CustomEditText, text: String?)
    {
        val old = v.et_input_text.text.toString()
        if (old != text)
        {
            v.et_input_text.setText(text)
        }
    }

    @JvmStatic
    @BindingAdapter("contentAttrChanged")
    fun setCustomEtInverseBindingListener(v: CustomEditText, listener: InverseBindingListener?)
    {
        v.et_input_text.addTextChangedListener(
            object : TextWatcher
            {
                override fun beforeTextChanged(
                    charSequence: CharSequence,
                    i: Int,
                    i1: Int,
                    i2: Int
                )
                {
                }

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int)
                {
                }

                override fun afterTextChanged(editable: Editable)
                {
                    listener?.onChange()
                }
            }
        )
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "content", event = "contentAttrChanged")
    fun getContent(v: CustomEditText): String
    {
        return v.et_input_text.text.toString()
    }

    //----------------------------------------------------------------------------------------------

    @JvmStatic
    @BindingAdapter("customEnable")
    fun setCustomEtViewBlock(v: CustomEditText, isBlock: Boolean) {
       v.et_input_text.isEnabled = isBlock
    }

    @JvmStatic
    @BindingAdapter("customOnClickListener")
    fun setCustomEtViewClickListener(v: CustomEditText, listener: View.OnClickListener) {
        v.et_input_text.setOnClickListener(listener)
    }

    @JvmStatic
    @BindingAdapter("descriptionText")
    fun bindCustomEditTextDescriptionText(
        ce: CustomEditText,
        text: String
    )
    {
        ce.setTvDescriptionText(text)
    }

    @JvmStatic
    @BindingAdapter("descriptionVisible")
    fun bindCustomEditTextDescriptionVisible(
        ce: CustomEditText,
        viewType: Int
    )
    {
        ce.setDescriptionVisible(viewType)
    }

    @JvmStatic
    @BindingAdapter("markVisible")
    fun bindCustomEditTextMarkVisible(
        ce: CustomEditText,
        viewType: Int
    )
    {
        ce.setMarkVisible(viewType)
    }

    @JvmStatic
    @BindingAdapter("type", "customFocusChangeListener")
    fun bindCustomFocusChangeListener(
        ce: CustomEditText,
        type: String,
        cb: ((String, Boolean) -> Unit)
    )
    {
        ce.setOnFocusChangeListener {
            cb.invoke(type, it)
        }
    }
}