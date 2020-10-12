package com.delivery.sopo.util.ui_util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.delivery.sopo.R
import kotlinx.android.synthetic.main.loading.*
import kotlinx.android.synthetic.main.loading.view.*

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
        if(!isAdded)
            show(act.supportFragmentManager, null)
    }

    fun onCloseDialog()
    {
        if(isAdded)
            dismiss()
    }

    private fun setSetting() {
        isCancelable = false
        dialog?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }
    }
}