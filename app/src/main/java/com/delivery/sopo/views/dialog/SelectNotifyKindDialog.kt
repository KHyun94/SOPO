package com.delivery.sopo.views.dialog

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.delivery.sopo.R
import com.delivery.sopo.databinding.SelectNotifyKindDialogBinding

class SelectNotifyKindDialog() : DialogFragment()
{
    private lateinit var binding: SelectNotifyKindDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SelectNotifyKindDialogBinding.inflate(inflater)
        setSetting()
        setClickEvent()

        return binding.root
    }

    private fun setClickEvent(){
        binding.tvClose.setOnClickListener {
            dismiss()
        }
    }

    private fun setSetting() {
        isCancelable = true
        dialog?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }
    }
}
