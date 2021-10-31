package com.delivery.sopo.util.ui_util

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

class CustomProgressBar(private val act: FragmentActivity): DialogFragment()
{
    private var isTurnOn = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.loading, container, false)
        setSetting()
        return view
    }

    fun onStartLoading()
    {
        show(act.supportFragmentManager, "isLoading")
    }

    fun onStopLoading()
    {
        dismiss()
    }


    fun onStartProgress(isProgress: Boolean, callback: (Boolean) -> Unit)
    {

        SopoLog.d("onStartProgress() call")

        if(isProgress && !isTurnOn && !isAdded)
        {
            activity?.run {
                show(supportFragmentManager, "isLoading")
            }
            SopoLog.d("Turn On ProgressBar")
            isTurnOn = true
            return
        }

        if(!isProgress && isTurnOn)
        {
            SopoLog.d("Turn Off ProgressBar")
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
    }
}