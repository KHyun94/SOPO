package com.delivery.sopo.util.ui_util

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.loading, container, false)
        setSetting()
        return view
    }

    fun onStartDialog()
    {
        if(!isAdded)
        {
            show(act.supportFragmentManager, null)
            SopoLog.d( "프로그레스 정상 시작")
        }
    }

    fun onCloseDialog()
    {
        if(isVisible)
        {
            SopoLog.d( "프로그레스 정상 취소")
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