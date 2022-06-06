package com.delivery.sopo.util.ui_util

import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.delivery.sopo.R
import com.delivery.sopo.databinding.SnackBarCustomBinding
import com.delivery.sopo.enums.SnackBarEnum
import com.google.android.material.snackbar.Snackbar

typealias OnSnackBarClickListener<T> = (data:T)->Unit

class CustomSnackBar<T>(private val view: View, private val content: String, private val data:T,  private val clickListener: Pair<CharSequence, OnSnackBarClickListener<T>>? = null, private val duration: Int, private val type: SnackBarEnum? = null)
{
    companion object
    {
        fun<T> make(view: View, content: String, data: T, duration: Int, type: SnackBarEnum? = null, clickListener: Pair<CharSequence, OnSnackBarClickListener<T>>? = null) =
            CustomSnackBar<T>(view = view, content = content, duration = duration, type = type, data = data, clickListener = clickListener)
    }

    private lateinit var binding: SnackBarCustomBinding
    private val snackBar = Snackbar.make(view, content, duration)
    private val snackBarLayout = snackBar.view as Snackbar.SnackbarLayout

    init
    {
        bindView()
        initUI(type = type)
        initData(content = content, data = data, clickListener = clickListener)
    }

    private fun bindView()
    {
        binding = DataBindingUtil.inflate(LayoutInflater.from(view.context), R.layout.snack_bar_custom, null, false)

        with(snackBarLayout) {
            setPadding(0, 0, 0, 0)
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            addView(binding.root, 0)
        }
    }

    private fun initUI(type: SnackBarEnum?)
    {
        type ?: return

        when(type)
        {
            SnackBarEnum.COMMON ->
            {
                binding.ivExclamationMark.background = ContextCompat.getDrawable(view.context, R.drawable.ic_exclamation_mark_blue)
                binding.layoutSnackBar.setBackgroundColor(ContextCompat.getColor(view.context, R.color.COLOR_MAIN_700))
            }
            SnackBarEnum.CONFIRM_DELETE ->
            {
                binding.ivExclamationMark.background = ContextCompat.getDrawable(view.context, R.drawable.ic_checked_deep_blue_small)
                binding.layoutSnackBar.setBackgroundColor(ContextCompat.getColor(view.context, R.color.COLOR_MAIN_100))
                binding.tvCountOfDeleted.setTextColor(ContextCompat.getColor(view.context, R.color.COLOR_MAIN_700))
                binding.tvCancelDelete.setTextColor(ContextCompat.getColor(view.context, R.color.COLOR_MAIN_700))
            }
            SnackBarEnum.ERROR ->
            {
                binding.ivExclamationMark.background = ContextCompat.getDrawable(view.context, R.drawable.ic_exclamation_mark_gray_scale)
                binding.layoutSnackBar.setBackgroundColor(ContextCompat.getColor(view.context, R.color.COLOR_GRAY_800))
            }
        }

        val slideUp: Animation = AnimationUtils.loadAnimation(view.context, R.anim.slide_up)
        val slideDown: Animation = AnimationUtils.loadAnimation(view.context, R.anim.slide_down)

        binding.layoutSnackBar.startAnimation(slideUp)

    }

    private fun initData(content: String, data: T, clickListener: Pair<CharSequence, OnSnackBarClickListener<T>>? = null)
    {
        binding.content = content

        clickListener?.run {

            binding.btnContent = first
            binding.tvCancelDelete.setOnClickListener {
                second.invoke(data)
                dismiss()
            }
        }
    }

    fun show()
    {
        snackBar.show()
    }

    fun dismiss()
    {
        snackBar.dismiss()
    }
}
