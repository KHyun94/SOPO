package com.delivery.sopo.util

import android.content.ComponentCallbacks
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import com.delivery.sopo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.function.Function

typealias OnTextClickListener = Pair<String?, View.OnClickListener?>

object AlertUtil
{
    var alert : AlertDialog? = null

    fun updateValueDialog(
        context: Context,
        okListener: OnTextClickListener?,
        cancelListener: OnTextClickListener?,
        callback : Function<String, Unit>
    )
    {
        val constraintLayout =
            View.inflate(context, R.layout.alert_update_dialog, null) as ConstraintLayout

        val layoutMain: ConstraintLayout = constraintLayout.findViewById(R.id.layout_main)
        val etInputText: EditText = constraintLayout.findViewById(R.id.et_input_text)
        val vFocusStatus: View = constraintLayout.findViewById(R.id.v_focus_status)
        val tvCancelBtn: TextView = constraintLayout.findViewById(R.id.tv_cancel_btn)
        val tvOkBtn: TextView = constraintLayout.findViewById(R.id.tv_ok_btn)


        alert = AlertDialog.Builder(context)
            .setView(constraintLayout)
            .setCancelable(false)
            .create()

        // 테두리 라운딩 처리
        alert!!.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }

        CoroutineScope(Dispatchers.Main).launch {

            tvOkBtn.text = okListener?.first ?: "확인"
            if (okListener?.first != null) tvOkBtn.setOnClickListener(okListener.second)
            else
            {
                tvCancelBtn.visibility = View.GONE
                tvOkBtn.setOnClickListener { alert!!.dismiss() }
            }

            if (cancelListener?.first != null)
            {
                tvCancelBtn.text = cancelListener.first

                if (cancelListener.second != null) tvCancelBtn.setOnClickListener(cancelListener.second)
                else tvCancelBtn.setOnClickListener { alert!!.dismiss() }
            }

            tvOkBtn.setTextColor(context.resources.getColor(R.color.COLOR_GRAY_200))
            etInputText.addTextChangedListener {
                if (it.toString().length > 1)
                {
                    tvOkBtn.setTextColor(context.resources.getColor(R.color.COLOR_MAIN_BLUE_700))
                }
            }

            tvOkBtn.setTextColor(context.resources.getColor(R.color.COLOR_GRAY_200))
            etInputText.addTextChangedListener {
                if(it.toString().isNotEmpty()){
                    tvOkBtn.setTextColor(context.resources.getColor(R.color.COLOR_MAIN_BLUE_700))
                    vFocusStatus.setBackgroundColor(context.resources.getColor(R.color.COLOR_MAIN_BLUE_700))
                    callback.apply(it.toString())
                }
                else
                {
                    tvOkBtn.setTextColor(context.resources.getColor(R.color.COLOR_GRAY_200))
                    vFocusStatus.setBackgroundColor(context.resources.getColor(R.color.COLOR_GRAY_200))
                }
            }
        }

        alert!!.show()
    }

    fun onDismiss()
    {
        if(alert != null)
        {
            alert!!.dismiss()
            alert = null
        }
    }
}