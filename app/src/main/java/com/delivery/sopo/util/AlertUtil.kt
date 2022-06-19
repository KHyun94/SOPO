package com.delivery.sopo.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.delivery.sopo.R
import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.databinding.AlertUpdateDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.function.Function

typealias OnTextClickListener = Pair<String?, View.OnClickListener?>

object AlertUtil: KoinComponent
{
    val appDataBase: AppDatabase by inject()

    var alert: AlertDialog? = null

    fun updateValueDialog(context: Context, title: String, okListener: OnTextClickListener?, cancelListener: OnTextClickListener?, callback: Function<String, Unit>)
    {
        val binding = DataBindingUtil.inflate<AlertUpdateDialogBinding>(LayoutInflater.from(context), R.layout.alert_update_dialog, null, false)

        alert = AlertDialog.Builder(context).setView(binding.constraintMainAlert).setCancelable(false).create()

        // 테두리 라운딩 처리
        alert?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }

/*        val param = alert?.window?.attributes
        param?.let { param ->
            param.width = SizeUtil.changePxToDp(context, 288.0f).toInt()
            param.height = SizeUtil.changePxToDp(context, 288.0f).toInt()
            alert?.window?.attributes = param
        }*/

        alert?.window?.setLayout((context.resources.displayMetrics.widthPixels * 0.9).toInt(), (context.resources.displayMetrics.heightPixels * 0.9).toInt())

        CoroutineScope(Dispatchers.Main).launch {

            binding.tvTitle.text = title

            binding.tvOkBtn.text = okListener?.first ?: "확인"
            if (okListener?.first != null) binding.tvOkBtn.setOnClickListener(okListener.second)
            else
            {
                binding.tvCancelBtn.visibility = View.GONE
                binding.tvOkBtn.setOnClickListener { alert!!.dismiss() }
            }

            if (cancelListener?.first != null)
            {
                binding.tvCancelBtn.text = cancelListener.first

                if (cancelListener.second != null) binding.tvCancelBtn.setOnClickListener(cancelListener.second)
                else binding.tvCancelBtn.setOnClickListener { alert!!.dismiss() }
            }

            binding.tvOkBtn.setTextColor(context.resources.getColor(R.color.COLOR_GRAY_200))
            binding.etInputText.addTextChangedListener {
                if (it.toString().length > 1)
                {
                    binding.tvOkBtn.setTextColor(context.resources.getColor(R.color.COLOR_MAIN_700))
                }
            }

            binding.tvOkBtn.setTextColor(context.resources.getColor(R.color.COLOR_GRAY_200))
            binding.etInputText.addTextChangedListener {
                if (it.toString().isNotEmpty())
                {
                    binding.tvOkBtn.setTextColor(context.resources.getColor(R.color.COLOR_MAIN_700))
                    binding.vFocusStatus.setBackgroundColor(context.resources.getColor(R.color.COLOR_MAIN_700))
                    callback.apply(it.toString())
                }
                else
                {
                    binding.tvOkBtn.setTextColor(context.resources.getColor(R.color.COLOR_GRAY_200))
                    binding.vFocusStatus.setBackgroundColor(context.resources.getColor(R.color.COLOR_GRAY_200))
                }
            }
        }

        alert!!.show()
    }

    fun onDismiss()
    {
        if (alert != null)
        {
            alert?.dismiss()
            alert = null
        }
    }
}