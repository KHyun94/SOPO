package com.delivery.sopo.util.ui_util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.delivery.sopo.R
import com.delivery.sopo.databinding.CustomToastMsgBinding
import com.delivery.sopo.util.SopoLog

object CustomAlertMsg
{
    val TAG = "LOG.SOPO"

    fun floatingUpperSnackBAr(context : Context, msg : String, isClick : Boolean){

        val inflater = LayoutInflater.from(context)

        var binding : CustomToastMsgBinding = DataBindingUtil.inflate(inflater, R.layout.custom_toast_msg, null, false)

        binding.msg = msg

        var t : Toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);

        t.setGravity(Gravity.CENTER or Gravity.TOP, 0, 50)
        t.view = binding.root

        if(isClick){
            binding.ivClear.setOnClickListener {
                SopoLog.d(msg = "click click click click click click click click click click")
                t.cancel()
            }

        }

        t.show()
    }
}