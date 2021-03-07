package com.delivery.sopo.views.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.util.SizeUtil
import com.delivery.sopo.util.SopoLog
import kotlinx.android.synthetic.main.custom_edit_text.view.*


class CustomEditText : LinearLayout
{

    private val TAG = "LOG.SOPO.CustomEt"

    private var text: String? = null
    private var title: String? = null
    private var descriptionText: String? = null
    private var hint: String? = null
    private var inputType: Int? = null
    private var nonFocusColor: Int? = null
    private var focusColor: Int? = null

    private var focusChangeColor = resources.getColor(R.color.COLOR_GRAY_200)
    private var underLineWidth: Int = ViewGroup.LayoutParams.MATCH_PARENT
    private var underLineHeight: Int = SizeUtil.changeSpToPx(SOPOApp.INSTANCE, 2.0f)

    constructor(context: Context?) : super(context)
    {
        initSetting(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    {
        initSetting(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
    {
        initSetting(context, attrs)
    }

    private fun initSetting(context: Context?, attrs: AttributeSet?)
    {
        val view = View.inflate(context, R.layout.custom_edit_text, this)

        if (attrs != null)
        {
            val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.CustomEditText, 0, 0)
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
                resources.getColor(R.color.COLOR_MAIN_700)
            )

            val test =
                typedArray?.getResourceId(R.styleable.CustomEditText_android_nextFocusDown, 0)
            et_input_text.nextFocusDownId = test ?: 0
        }

        et_input_text.setText(text ?: "")
        tv_title.text = title ?: ""
        tv_description_text.text = descriptionText ?: ""
        et_input_text.hint = hint ?: ""
        et_input_text.inputType = inputType ?: EditorInfo.TYPE_CLASS_TEXT
        v_underline.setBackgroundColor(nonFocusColor!!)

        et_input_text.setOnFocusChangeListener { view, focus ->

            focusChangeColor = if (focus)
            {
                focusColor ?: resources.getColor(R.color.FOCUS_ON)
            }
            else
            {
                nonFocusColor ?: resources.getColor(R.color.FOCUS_OFF)
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

    fun setTitle(title: String)
    {
        tv_title.text = title
    }

    fun getTitle(): String
    {
        return tv_title.text.toString()
    }

    fun setText(text: String)
    {
        et_input_text.setText(text)
    }

    fun getText(): String
    {
        return et_input_text.text.toString()
    }

    fun setTvDescriptionText(err: String)
    {
        tv_description_text.text = err
    }

    fun setDescriptionVisible(visibleStatus: Int)
    {
        iv_description_mark.visibility = visibleStatus
        tv_description_text.visibility = visibleStatus
    }

    fun setMarkVisible(visibleStatus: Int)
    {
        iv_right_mark.visibility = visibleStatus
    }

    fun setHint(hint: String)
    {
        et_input_text.hint = hint
    }

    fun setOnFocusChangeListener(cb: (Boolean) -> Unit)
    {
        et_input_text.setOnFocusChangeListener { v, b ->

            focusChangeColor = if (b)
            {
                focusColor ?: resources.getColor(R.color.COLOR_MAIN_700)
            }
            else
            {
                nonFocusColor ?: resources.getColor(R.color.COLOR_GRAY_200)
            }

            val lp = RelativeLayout.LayoutParams(
                underLineWidth, underLineHeight
            )

            lp.addRule(RelativeLayout.BELOW, et_input_text.id)
            v_underline.run {
                layoutParams = lp
                setBackgroundColor(focusChangeColor)
            }
            tv_title.setTextColor(focusChangeColor)

            cb.invoke(b)
        }
    }

    companion object{
        const val STATUS_COLOR_RED = 0
        const val STATUS_COLOR_BLUE = 1
        const val STATUS_COLOR_BLACK = 2
        const val STATUS_COLOR_ELSE = -1

    }

    fun updateStatusColor(type: Int)
    {
        when (type)
        {
            0 ->
            {
                SopoLog.d( msg = "Red")
                tv_title.setTextColor(resources.getColor(R.color.COLOR_MAIN_RED_500))
                v_underline.setBackgroundResource(R.color.COLOR_MAIN_RED_500)
            }
            1 ->
            {
                SopoLog.d( msg = "Blue")
                tv_title.setTextColor(resources.getColor(R.color.COLOR_MAIN_700))
                v_underline.setBackgroundResource(R.color.COLOR_MAIN_700)

            }
            2 ->
            {

                SopoLog.d( msg = "Black")
                tv_title.setTextColor(resources.getColor(R.color.MAIN_BLACK))
                v_underline.setBackgroundResource(R.color.MAIN_BLACK)
            }
            else ->
            {
                SopoLog.d( msg = "Black & Gray")
                tv_title.setTextColor(resources.getColor(R.color.MAIN_BLACK))
                v_underline.setBackgroundResource(R.color.COLOR_GRAY_200)
            }
        }
    }

    fun setOnClearListener(context: Context?)
    {
        if (context != null)
        {
            Glide.with(context)
                .load(R.drawable.ic_clear_btn)
                .into(iv_right_mark)

            et_input_text.addTextChangedListener(
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
                        if (editable.toString().isNotEmpty()) iv_right_mark.visibility =
                            View.VISIBLE
                        else iv_right_mark.visibility = View.GONE
                    }
                }
            )

            iv_right_mark.setOnClickListener {
                et_input_text.setText("")
                tv_title.setTextColor(resources.getColor(R.color.MAIN_BLACK))
                v_underline.setBackgroundResource(R.color.COLOR_GRAY_200)
            }
        }
        else
        {
            iv_right_mark.run {
                setBackgroundResource(0)
                setOnClickListener(null)
            }
        }

    }

    fun setOnKeyListener(cb: ((View, Int, KeyEvent) -> Unit))
    {
        et_input_text.setOnKeyListener { v, keyCode, event ->
            cb.invoke(v, keyCode, event)
            return@setOnKeyListener false
        }
    }

    fun etClearFocus()
    {
        et_input_text.clearFocus()
    }
}