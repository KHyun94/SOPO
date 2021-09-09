package com.delivery.sopo.views.widget

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.delivery.sopo.R
import com.delivery.sopo.databinding.AlertMessageBarBinding
import com.delivery.sopo.util.OtherUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlertMessageBar : RelativeLayout
{
    private lateinit var view: View
    private lateinit var layoutMain: RelativeLayout
    private lateinit var tvContent: TextView
    private lateinit var ivMarker: ImageView
    private lateinit var tvButton: TextView

    private var contentStr: String? = null
    private var buttonStr: String? = null
    private var markImg: Int = R.drawable.ic_blue_marker
    private var bgColor: Int = R.color.COLOR_MAIN_700
    private var contentTextColor: Int = R.color.MAIN_WHITE
    private var buttonTextColor: Int = R.color.MAIN_WHITE

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
        val binding: AlertMessageBarBinding = AlertMessageBarBinding.inflate(LayoutInflater.from(context))
//        view = View.inflate(context, R.layout.alert_message_bar, this)

        layoutMain = binding.layoutMain
        ivMarker = binding.ivMarker
        tvContent = binding.tvContent
        tvButton = binding.tvButton

        if (attrs != null)
        {
            val typedArray =
                context?.obtainStyledAttributes(attrs, R.styleable.AlertMessageBar, 0, 0)

            contentStr = typedArray?.getString(R.styleable.AlertMessageBar_msgtext) ?: "메시지"
            buttonStr = typedArray?.getString(R.styleable.AlertMessageBar_buttonText) ?: "버튼"
            markImg = typedArray?.getResourceId(
                R.styleable.AlertMessageBar_markImage,
                R.drawable.ic_blue_marker
            ) ?: R.drawable.ic_blue_marker
            bgColor = typedArray?.getResourceId(
                R.styleable.AlertMessageBar_customBgColor,
                R.color.COLOR_MAIN_700
            ) ?: R.color.COLOR_MAIN_700
            contentTextColor =
                typedArray?.getResourceId(R.styleable.AlertMessageBar_textColor, R.color.MAIN_WHITE)
                    ?: R.color.MAIN_WHITE
            buttonTextColor = typedArray?.getResourceId(
                R.styleable.AlertMessageBar_buttonTextColor,
                R.color.MAIN_WHITE
            ) ?: R.color.MAIN_WHITE

            layoutMain.visibility = View.GONE

            CoroutineScope(Dispatchers.Main).launch {
                layoutMain.setBackgroundResource(bgColor)
                tvContent.run {
                    text = contentStr
                    setTextColor(resources.getColor(contentTextColor))
                }

                tvButton.run {
                    text = buttonStr
                    setTextColor(resources.getColor(buttonTextColor))
                }

                Glide.with(ivMarker.context).load(markImg).into(ivMarker)

                tvContent.setTextColor(resources.getColor(contentTextColor))
                tvButton.setTextColor(resources.getColor(buttonTextColor))
            }

        }
    }

    fun setLeftMarkResource(@DrawableRes res: Int)
    {
        try
        {
            val mimeTypedValue = OtherUtil.getResourceExtension(res)

            when (mimeTypedValue)
            {
                "gif" ->
                {
                    Glide.with(view.context)
                        .asGif()
                        .load(res)
                        .into(ivMarker)
                }
                else ->
                {
                    Glide.with(view.context)
                        .load(res)
                        .into(ivMarker)
                }
            }
        }
        catch (e: Exception)
        {
            Glide.with(view.context)
                .load(res)
                .into(ivMarker)
        }
    }

    fun setText(msg: String)
    {
        tvContent.text = msg
    }

    fun setTextColor(colorHex: Int)
    {
        tvContent.setTextColor(resources.getColor(colorHex))
    }

    fun setBackgroundRes(resid: Int)
    {
        layoutMain.setBackgroundResource(resid)
    }

    fun setOnCancelClicked(clickText: String?, clickTextColor: Int?, listener: OnClickListener?)
    {
        CoroutineScope(Dispatchers.Main).launch {
            tvButton.run {
                text = clickText ?: buttonStr
                setTextColor(context.getColor(clickTextColor ?: buttonTextColor))
                setOnClickListener(listener)
            }
        }
    }

    fun onStart(timer: Long = 5000)
    {
        layoutMain.visibility = View.VISIBLE

        Handler().postDelayed(Runnable {
            layoutMain.visibility = View.GONE
        }, timer ?: 5000)
    }

    fun onDismiss()
    {
        layoutMain.visibility = View.GONE
    }
}