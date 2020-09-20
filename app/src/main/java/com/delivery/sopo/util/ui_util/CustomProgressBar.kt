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
import com.bumptech.glide.Glide
import com.delivery.sopo.R
import kotlinx.android.synthetic.main.loading.*
import kotlinx.android.synthetic.main.loading.view.*

class CustomProgressBar(private val act: AppCompatActivity) : DialogFragment()
{
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        val view = inflater.inflate(R.layout.loading, container, false)
        setSetting()
        setGiFImage(view)
        return view
    }



    fun onStartDialog()
    {
        show(act.supportFragmentManager, null)
    }

    fun onCloseDialog()
    {
        dismiss()
    }

    private fun setSetting() {
        isCancelable = false
        dialog?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }
    }

    private fun setGiFImage(v:View){
//        Glide
//            .with(v.iv_loading.context)
//            .asGif()
//            .load(R.drawable.ic_loading)
//            .into(v.iv_loading)

    }

    override fun onCancel(dialog: DialogInterface)
    {
        super.onCancel(dialog)
    }
}