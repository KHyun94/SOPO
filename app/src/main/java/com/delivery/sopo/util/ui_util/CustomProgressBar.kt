package com.delivery.sopo.util.ui_util

import android.app.Dialog
import android.content.Context
import android.view.Window
import com.bumptech.glide.Glide
import com.delivery.sopo.R
import kotlinx.android.synthetic.main.loading.*

class CustomProgressBar(context: Context) : Dialog(context)
{
    init
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.loading)

        Glide.with(iv_loading.context)
            .load(R.drawable.ic_loading)
            .into(iv_loading)
    }

    fun onStartDialog()
    {
        show()
    }

    fun onCloseDialog()
    {
        dismiss()
    }
}