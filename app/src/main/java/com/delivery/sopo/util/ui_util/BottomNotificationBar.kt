package com.delivery.sopo.util.ui_util

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.delivery.sopo.R
import com.delivery.sopo.databinding.BottomNotificationBarBinding
import com.delivery.sopo.enums.SnackBarEnum
import com.delivery.sopo.enums.SnackBarType
import com.delivery.sopo.extensions.makeGone
import com.delivery.sopo.extensions.makeVisible
import com.delivery.sopo.util.AnimationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BottomNotificationBar: ConstraintLayout
{
    private lateinit var binding: BottomNotificationBarBinding

    private var duration: Long = 3000

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

    fun make(snackBarType: SnackBarType): BottomNotificationBar
    {
        setButton()

        when(snackBarType)
        {
            is SnackBarType.Common ->
            {
                binding.ivIconStart.setAnimation(R.raw.lottie_empty)
                binding.ivIconStart.background =
                    ContextCompat.getDrawable(context, R.drawable.ic_exclamation_mark_blue)
                binding.constraintMainBottomNotiBar.setBackgroundColor(ContextCompat.getColor(context, R.color.COLOR_MAIN_700))

                binding.tvContent.text = snackBarType.content
                duration = snackBarType.duration

                setButton(snackBarType.buttonContent?:"", snackBarType.clickListener, snackBarType.iconRes)
            }
            is SnackBarType.Update ->
            {
                binding.ivIconStart.setAnimation(R.raw.lottie_empty)
                binding.ivIconStart.background = ContextCompat.getDrawable(context, R.drawable.ic_checked_deep_blue_small)
                binding.constraintMainBottomNotiBar.setBackgroundColor(ContextCompat.getColor(context, R.color.COLOR_MAIN_100))
                binding.tvContent.setTextColor(ContextCompat.getColor(context, R.color.COLOR_MAIN_700))

                binding.tvContent.text = snackBarType.content
                duration = snackBarType.duration

                binding.tvEvent.setTextColor(ContextCompat.getColor(context, R.color.COLOR_MAIN_700))
                setButton(snackBarType.buttonContent?:"", snackBarType.clickListener, snackBarType.iconRes)
            }
            is SnackBarType.ConfirmDelete ->
            {
                binding.ivIconStart.setAnimation(R.raw.lottie_empty)
                binding.ivIconStart.background =
                    ContextCompat.getDrawable(context, R.drawable.ic_checked_deep_blue_small)
                binding.constraintMainBottomNotiBar.setBackgroundColor(ContextCompat.getColor(context, R.color.COLOR_MAIN_100))
                binding.tvContent.setTextColor(ContextCompat.getColor(context, R.color.COLOR_MAIN_700))
                binding.tvEvent.setTextColor(ContextCompat.getColor(context, R.color.COLOR_MAIN_700))
                binding.tvEvent.typeface = ResourcesCompat.getFont(context, R.font.pretendard_bold)

                binding.tvContent.text = snackBarType.content
                duration = snackBarType.duration

            }
            is SnackBarType.ConnectNetwork ->
            {
                binding.ivIconStart.setAnimation(R.raw.lottie_empty)
                binding.ivIconStart.background =
                    ContextCompat.getDrawable(context, R.drawable.ic_checked_deep_blue_small)
                binding.constraintMainBottomNotiBar.setBackgroundColor(ContextCompat.getColor(context, R.color.COLOR_MAIN_100))
                binding.tvContent.setTextColor(ContextCompat.getColor(context, R.color.COLOR_MAIN_700))

                binding.tvContent.text = snackBarType.content
                duration = snackBarType.duration
            }
            is SnackBarType.DisconnectNetwork ->
            {
                binding.ivIconStart.setAnimation(R.raw.lottie_network_lost)
                binding.ivIconStart.background = null
                binding.constraintMainBottomNotiBar.setBackgroundColor(ContextCompat.getColor(context, R.color.COLOR_GRAY_700))
                binding.tvContent.setTextColor(ContextCompat.getColor(context, R.color.COLOR_GRAY_100))

                binding.tvContent.text = snackBarType.content
                duration = snackBarType.duration
            }
            is SnackBarType.Error ->
            {
                binding.ivIconStart.setAnimation(R.raw.lottie_empty)
                binding.ivIconStart.background =
                    ContextCompat.getDrawable(context, R.drawable.ic_exclamation_mark_gray_scale)
                binding.constraintMainBottomNotiBar.setBackgroundColor(ContextCompat.getColor(context, R.color.COLOR_GRAY_800))
                binding.tvContent.setTextColor(ContextCompat.getColor(context, R.color.MAIN_WHITE))
                binding.tvEvent.setTextColor(ContextCompat.getColor(context, R.color.MAIN_WHITE))

                binding.tvContent.text = snackBarType.content
                duration = snackBarType.duration
            }
        }

        return this
    }

    fun setButton(btnContent: String= "", listener: OnClickListener? = null, btnIcon: Int? = null)
    {
        binding.tvEvent.text = btnContent
        binding.tvEvent.setOnClickListener(listener)

        if(btnIcon == null)
        {
            binding.ivIconEnd.visibility = View.GONE
        }
        else
        {
            binding.ivIconEnd.visibility = VISIBLE
            binding.ivIconEnd.background = ContextCompat.getDrawable(context, btnIcon)
        }
    }

    fun show() = CoroutineScope(Dispatchers.Main).launch {
        AnimationUtil.slideUp(binding.constraintMainBottomNotiBar)
        if(duration > 0) dismiss()
    }

    fun dismiss() = CoroutineScope(Dispatchers.Main).launch {
        delay(duration)
        AnimationUtil.slideDown(binding.constraintMainBottomNotiBar)

        binding.ivIconEnd.makeGone()
    }

    private fun setTypeArray(typedArray: TypedArray)
    {
        binding.constraintMainBottomNotiBar.setBackgroundResource(typedArray.getResourceId(R.styleable.BottomNotificationBar_notificationBackground, R.color.COLOR_GRAY_800))

        binding.tvContent.text = typedArray.getString(R.styleable.BottomNotificationBar_contentText)
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
        if(iconEnd != 0) binding.ivIconEnd.background = ContextCompat.getDrawable(context, iconEnd)
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
