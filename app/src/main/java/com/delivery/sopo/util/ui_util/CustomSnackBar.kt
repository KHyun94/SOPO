package com.delivery.sopo.util.ui_util

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.delivery.sopo.R
import com.delivery.sopo.databinding.SnackBarCustomBinding
import com.delivery.sopo.enums.SnackBarEnum
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CustomSnackBar(private val view: View,  private val content: String,  private val duration: Int, private val type: SnackBarEnum? = null,  private val clickListener: Pair<String, View.OnClickListener>? = null)
{
    companion object{
        fun make(view: View, content: String, duration: Int, type: SnackBarEnum? = null, clickListener: Pair<String, View.OnClickListener>? = null)
                = CustomSnackBar(view = view, content = content, duration = duration, type = type, clickListener = clickListener)
    }

    lateinit var binding: SnackBarCustomBinding
    private val snackbar = Snackbar.make(view, "", 5000)
    private val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout

    init
    {
        bindView()
        initUI(type = type)
        initData(content = content, clickListener = clickListener)
    }

    private fun bindView()
    {
        binding = SnackBarCustomBinding.inflate(LayoutInflater.from(view.context))

        with(snackbarLayout) {
//            removeAllViews()
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

    private fun initData(content: String, btnContent: String? = null, clickListener: Pair<String, View.OnClickListener>? = null)
    {
        binding.content = content

        clickListener?.run {
            binding.btnContent = first
            binding.clickListener = second
        }
    }

    fun show(){
        snackbar.show()
        //        binding.layoutSnackBar.visibility = View.VISIBLE
        //
        //        Handler(Looper.getMainLooper()).postDelayed(Runnable{
        //            binding.layoutSnackBar.visibility = View.GONE
        //        }, duration.toLong())
    }

    /*    fun floatingUpperSnackBAr(context : Context, msg : String, isClick : Boolean){

            val inflater = LayoutInflater.from(context)

            var binding : CustomToastMsgBinding = DataBindingUtil.inflate(inflater, R.layout.custom_toast_msg, null, false)

            binding.msg = msg

            var t : Toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);

            t.setGravity(Gravity.CENTER or Gravity.TOP, 0, 50)
            t.view = binding.root

            if(isClick){
                binding.ivClear.setOnClickListener {
                    t.cancel()
                }

            }

            t.show()
        }*/
}
