package com.delivery.sopo.util.ui_util

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.delivery.sopo.R
import com.delivery.sopo.databinding.BottomNotificationBarBinding
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BottomNotificationBar: ConstraintLayout
{
    private lateinit var binding: BottomNotificationBarBinding

    constructor(context: Context): this(context, null)
    {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    {
        init()
        getAttrs(attrs = attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    {
        init()
        getAttrs(attrs = attrs, defStyleAttr = defStyleAttr)
    }

    private fun init()
    {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = BottomNotificationBarBinding.inflate(inflater, this, false)
        addView(binding.root)
    }

    private fun getAttrs(attrs: AttributeSet?)
    {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BottomNotificationBar)
        setTypeArray(typedArray)
    }

    private fun getAttrs(attrs: AttributeSet?, defStyleAttr: Int)
    {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BottomNotificationBar, defStyleAttr, 0)

        setTypeArray(typedArray)
    }

    private fun setTypeArray(typedArray: TypedArray) {
 /*       binding.constraintMainBottomNotiBar.setBackgroundResource(typedArray.getResourceId(R.styleable.BottomNotificationBar_notificationBackground,R.color.COLOR_GRAY_800) )

        binding.tvContent.text = typedArray.getString(R.styleable.BottomNotificationBar_contentText).apply {
            SopoLog.d("BottomNavigator Text:$this")
        }
        binding.tvEvent.text = typedArray.getString(R.styleable.BottomNotificationBar_buttonText)

        binding.tvContent.setTextColor(ContextCompat.getColor(context, typedArray.getInt(R.styleable.BottomNotificationBar_textColor, R.color.COLOR_GRAY_800)))
        binding.tvEvent.setTextColor(ContextCompat.getColor(context, typedArray.getInt(R.styleable.BottomNotificationBar_buttonTextColor, R.color.MAIN_WHITE)))

        binding.tvContent.typeface =
            ResourcesCompat.getFont(context, typedArray.getInt(R.styleable.BottomNotificationBar_textFontFamily, R.font.pretendard_medium))
        binding.tvContent.typeface =
            ResourcesCompat.getFont(context, typedArray.getInt(R.styleable.BottomNotificationBar_textFontFamily, R.font.pretendard_medium))

        binding.ivIconStart.background =
            ContextCompat.getDrawable(context, typedArray.getInt(R.styleable.BottomNotificationBar_iconStart, R.drawable.ic_exclamation_mark_gray_scale))

        val iconEnd = typedArray.getInt(R.styleable.BottomNotificationBar_iconEnd, 0)
        if(iconEnd != 0) binding.ivIconEnd.background = ContextCompat.getDrawable(context, iconEnd)*/
    }

    /*    fun setIconStart(@DrawableRes resource: Int = R.drawable.ic_exclamation_mark_gray_scale){

            val iv = binding.ivIconStart

            Glide.with(context)
                .load(resource)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(iv)
        }

        fun setIconEnd(@DrawableRes resource: Int){

            val iv = binding.ivIconEnd

            Glide.with(context)
                .load(resource)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(iv)
        }*/
}
