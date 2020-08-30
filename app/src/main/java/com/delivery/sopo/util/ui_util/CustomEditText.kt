package com.delivery.sopo.util.ui_util

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.util.fun_util.SizeUtil
import kotlinx.android.synthetic.main.custom_edit_text.view.*


class CustomEditText : LinearLayout {

    private val TAG = "LOG.SOPO.CustomEt"

    private var text: String? = null
    private var title: String? = null
    private var descriptionText: String? = null
    private var hint: String? = null
    private var inputType: Int? = null
    private var nonFocusColor: Int? = null
    private var focusColor: Int? = null

    private var focusChangeColor = resources.getColor(R.color.FOCUS_OFF)
    private var underLineWidth: Int = ViewGroup.LayoutParams.MATCH_PARENT
    private var underLineHeight: Int = SizeUtil.changeSpToPx(SOPOApp.INSTANCE, 2.0f)

    constructor(context: Context?) : super(context) {
        initSetting(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initSetting(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initSetting(context, attrs)
    }

    private fun initSetting(context: Context?, attrs: AttributeSet?) {
        val view = View.inflate(context, R.layout.custom_edit_text, this)

        if (attrs != null) {
            val typedArray =
                context?.obtainStyledAttributes(attrs, R.styleable.CustomEditText, 0, 0)
            text = typedArray?.getString(R.styleable.CustomEditText_text)
            title = typedArray?.getString(R.styleable.CustomEditText_title)
            hint = typedArray?.getString(R.styleable.CustomEditText_hint)
            descriptionText = typedArray?.getString(R.styleable.CustomEditText_descriptionText)
            inputType = typedArray?.getInt(
                R.styleable.CustomEditText_android_inputType,
                EditorInfo.TYPE_CLASS_TEXT
            )
            nonFocusColor = typedArray?.getColor(
                R.styleable.CustomEditText_nonFocusColor,
                resources.getColor(R.color.COLOR_GRAY_200)
            )
            focusColor = typedArray?.getColor(
                R.styleable.CustomEditText_focusColor,
                resources.getColor(R.color.COLOR_MAIN_BLUE)
            )
        }

        et_input_text.setText(text ?: "")
        tv_title.text = title ?: ""
        tv_description_text.text = descriptionText ?: ""
        et_input_text.hint = hint ?: ""
        et_input_text.inputType = inputType ?: EditorInfo.TYPE_CLASS_TEXT
        v_underline.setBackgroundColor(nonFocusColor!!)

        et_input_text.setOnFocusChangeListener { view, focus ->

            if (focus) {
                focusChangeColor = focusColor ?: resources.getColor(R.color.FOCUS_ON)
            } else {
                focusChangeColor = nonFocusColor ?: resources.getColor(R.color.FOCUS_OFF)
            }

            val lp = RelativeLayout.LayoutParams(
                underLineWidth, underLineHeight
            )

            lp.addRule(RelativeLayout.BELOW, et_input_text.id)
            v_underline.layoutParams = lp
            v_underline.setBackgroundColor(focusChangeColor)
            tv_title.setTextColor(focusChangeColor)
        }
    }

    fun setTitle(title: String) {
        tv_title.text = title
    }

    fun getTitle(): String {
        return tv_title.text.toString()
    }

    fun setText(text: String) {
        et_input_text.setText(text)
    }

    fun getText(): String {
        return et_input_text.text.toString()
    }

    fun setTvDescriptionText(err: String) {
        tv_description_text.text = err
    }

    fun setDescriptionVisible(visibleStatus: Int) {
        iv_description_mark.visibility = visibleStatus
        tv_description_text.visibility = visibleStatus
    }

    fun setMarkVisible(visibleStatus: Int) {
        iv_right_mark.visibility = visibleStatus
    }

    fun setHint(hint: String) {
        et_input_text.hint = hint
    }

    fun setOnFocusChangeListener(cb: (Boolean) -> Unit) {
        et_input_text.setOnFocusChangeListener { v, b ->

            if (b) {
                focusChangeColor = focusColor ?: resources.getColor(R.color.FOCUS_ON)
            } else {
                focusChangeColor = nonFocusColor ?: resources.getColor(R.color.FOCUS_OFF)
            }

            val lp = RelativeLayout.LayoutParams(
                underLineWidth, underLineHeight
            )

            lp.addRule(RelativeLayout.BELOW, et_input_text.id)
            v_underline.layoutParams = lp
            v_underline.setBackgroundColor(focusChangeColor)
            tv_title.setTextColor(focusChangeColor)

            cb.invoke(b)
        }
    }
}