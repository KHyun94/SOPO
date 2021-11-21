package com.delivery.sopo.util.ui_util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.delivery.sopo.R
import com.delivery.sopo.util.SopoLog

class SopoLoadingBar: Dialog
{
    constructor(context: Context) : super(context) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.loading)
    }

    init {
        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }



    /*    fun onStartProgress(isProgress: Boolean, callback: (Boolean) -> Unit)
        {
            if(isProgress && !isTurnOn && !isAdded)
            {
                act.run {
                    show(act.supportFragmentManager, "isLoading")
                }
                isTurnOn = true
                return
            }

            if(!isProgress && isTurnOn)
            {
                isTurnOn = false
                callback.invoke(true)
                dismiss()
            }
        }

        private fun setSetting()
        {
            isCancelable = false
            dialog?.window?.run {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                requestFeature(Window.FEATURE_NO_TITLE)
            }
        }*/
}
