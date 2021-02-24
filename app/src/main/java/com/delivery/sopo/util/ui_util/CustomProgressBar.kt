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

class CustomProgressBar(private val act: FragmentActivity) : DialogFragment()
{
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        val view = inflater.inflate(R.layout.loading, container, false)
        setSetting()
        return view
    }

    fun onStartDialog()
    {
        if (!isAdded)
        {
            show(act.supportFragmentManager, null)
            Log.d("!!!!!!!", "프로그레스 정상 등록")
        }
        else
        {
            Log.d("!!!!!!!", "프로그레스 정상 등록 실패")
        }
    }

    fun onCloseDialog()
    {
        if (!isAdded)
        {
            Log.d("!!!!!!!", "프로그레스 정상 취소 실패")

        }
        else
        {
            Log.d("!!!!!!!", "프로그레스 정상 취소")
            dismiss()
        }
    }

    fun autoProgressbar(value : Boolean?){

        if(value == null) return

        if(value)
        {
            onStartDialog()
        }
        else
        {
            onCloseDialog()
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