package com.delivery.sopo.util.ui_util

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.delivery.sopo.R
import com.delivery.sopo.databinding.SnackBarCustomBinding
import com.delivery.sopo.enums.SnackBarEnum
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

typealias OnSnackBarClickListener = ()->Unit
class CustomSnackBar(private val view: View, private val content: String, private val duration: Int, private val type: SnackBarEnum? = null, private val clickListener: Pair<String, OnSnackBarClickListener>? = null)
{
    companion object
    {
        fun make(view: View, content: String, duration: Int, type: SnackBarEnum? = null, clickListener: Pair<String, OnSnackBarClickListener>? = null) =
            CustomSnackBar(view = view, content = content, duration = duration, type = type, clickListener = clickListener)
    }

    private lateinit var binding: SnackBarCustomBinding
    private val snackbar = Snackbar.make(view, content, duration)
    private val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout

    init
    {
        bindView()
        initUI(type = type)
        initData(content = content, clickListener = clickListener)
    }

    private fun bindView()
    {
        binding = DataBindingUtil.inflate(LayoutInflater.from(view.context), R.layout.snack_bar_custom, null, false)

        with(snackbarLayout) {
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
                binding.ivExclamationMark.background = ContextCompat.getDrawable(view.context, R.drawable.ic_checked_blue_300)
                binding.layoutSnackBar.setBackgroundColor(ContextCompat.getColor(view.context, R.color.COLOR_MAIN_100))
                binding.tvCountOfDeleted.setTextColor(ContextCompat.getColor(view.context, R.color.COLOR_MAIN_900))
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

    private fun initData(content: String, btnContent: String? = null, clickListener: Pair<String, OnSnackBarClickListener>? = null)
    {
        binding.content = content

        clickListener?.run {
            binding.btnContent = first
            binding.tvCancelDelete.setOnClickListener {
                second.invoke()
                dismiss()
            }
        }
    }

    fun show()
    {
        snackbar.show()
    }

    fun dismiss()
    {
        snackbar.dismiss()
    }
}
