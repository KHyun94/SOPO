package com.delivery.sopo.views.dialog

import android.app.ActionBar
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.delivery.sopo.R
import com.delivery.sopo.databinding.DialogLogoutBinding
import com.delivery.sopo.databinding.GeneralDialogBinding
import com.delivery.sopo.util.SizeUtil

class LogoutDialog : DialogFragment
{
    private var parentActivity: Activity

    private lateinit var binding: DialogLogoutBinding

    private val onConfirmClickListener: View.OnClickListener

    constructor(
        act: Activity,
        onConfirmClickListener: View.OnClickListener
    ) : super()
    {
        this.parentActivity = act
        this.onConfirmClickListener = onConfirmClickListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = DialogLogoutBinding.inflate(inflater, container, false)

        setSetting()
        setClickEvent()

        return binding.root
    }

    override fun onResume()
    {
        super.onResume()
        dialog?.window?.setLayout(
            (SizeUtil.getDeviceSize(parentActivity).x * 4 / 5),
            ActionBar.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setClickEvent()
    {
        binding.tvConfirm.setOnClickListener(onConfirmClickListener)
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
