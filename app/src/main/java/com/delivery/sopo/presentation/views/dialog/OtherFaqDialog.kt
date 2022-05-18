package com.delivery.sopo.presentation.views.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.delivery.sopo.databinding.OtherFaqDialogBinding


class OtherFaqDialog(): DialogFragment()
{
    private lateinit var binding: OtherFaqDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = OtherFaqDialogBinding.inflate(inflater)
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
