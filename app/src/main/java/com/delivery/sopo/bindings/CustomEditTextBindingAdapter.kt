package com.delivery.sopo.bindings

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.delivery.sopo.models.ValidateResult
import com.delivery.sopo.util.ui_util.CustomEditText
import com.delivery.sopo.util.fun_util.ValidateUtil
import kotlinx.android.synthetic.main.custom_edit_text.view.*

object CustomEditTextBindingAdapter {

    // 커스텀 뷰 two-binding
    @JvmStatic
    @BindingAdapter("content")
    fun setCustomEtView(v: CustomEditText, content: String?) {
        val old = v.et_input_text.text.toString()
        if (old != content) {
            v.et_input_text.setText(content)
        }
    }

    @JvmStatic
    @BindingAdapter("contentAttrChanged")
    fun setCustomEtInverseBindingListener(v: CustomEditText, listener: InverseBindingListener?) {
        v.et_input_text.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence,
                    i: Int,
                    i1: Int,
                    i2: Int
                ) {
                }

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                }

                override fun afterTextChanged(editable: Editable) {
                    listener?.onChange()
                }
            }
        )
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "content", event = "contentAttrChanged")
    fun getContent(v: CustomEditText): String {
        return v.et_input_text.text.toString()
    }

    // ---------------------------------------------------------------------------------------------
    @JvmStatic
    @BindingAdapter("setDescriptionText")
    fun bindCustomEditTextDescriptionText(
        ce: CustomEditText,
        text:String
    ){
        ce.setTvDescriptionText(text)
    }

    @JvmStatic
    @BindingAdapter("setDescriptionVisible")
    fun bindCustomEditTextDescriptionVisible(
        ce: CustomEditText,
        viewType:Int
    ){
        ce.setDescriptionVisible(viewType)
    }

    @JvmStatic
    @BindingAdapter("setMarkVisible")
    fun bindCustomEditTextMarkVisible(
        ce: CustomEditText,
        viewType:Int
    ){
        ce.setMarkVisible(viewType)
    }

    @JvmStatic
    @BindingAdapter("type","setCustomFocusChangeListener")
    fun bindCustomFocusChangeListener(
        ce: CustomEditText,
        type:String,
        cb: ((String, Boolean) -> Unit)
    ) {
        ce.setOnFocusChangeListener{
            cb.invoke(type, it)
        }
    }

    @JvmStatic
    @BindingAdapter("setClearFocus")
    fun bindCustomEditTextClearFocus(
        ce: CustomEditText,
        isClear:Boolean
    ){
        if(isClear)
            ce.et_input_text.clearFocus()
    }
}