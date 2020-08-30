package com.delivery.sopo.views.registers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.databinding.RegisterStep1Binding
import com.delivery.sopo.enums.FragmentType
import com.delivery.sopo.util.fun_util.ClipboardUtil
import com.delivery.sopo.util.fun_util.OtherUtil
import com.delivery.sopo.util.ui_util.FragmentManager
import com.delivery.sopo.viewmodels.registesrs.RegisterViewModel
import com.delivery.sopo.views.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterStep1 : Fragment()
{
    private lateinit var binding: RegisterStep1Binding
    private val registerVm: RegisterViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.register_step1, container, false)
        binding.vm = registerVm
        binding.lifecycleOwner = this

        setObserve()

        return binding.root
    }

    fun setObserve()
    {
//        binding.vm?.hideKeyboard?.observe(this, Observer {
//            if (it != null && it)
//            {
//                OtherUtil.hideKeyboardSoft(activity as MainView)
//                binding.vm?.hideKeyboard?.value = false
//            }
//        })

        binding.vm?.trackNumStr?.observe(this, Observer {
            if (it.isNotEmpty())
            {
                binding.vm?.clipboardStr?.value = ""
            }
        })

        binding.vm?.moveFragment?.observe(this, Observer {
            when (it.NAME)
            {
                FragmentType.REGISTER_STEP1.NAME ->
                {
                    FragmentManager.move(
                        activity!!,
                        FragmentType.REGISTER_STEP1,
                        RegisterMainFrame.viewId
                    )
                }


                FragmentType.REGISTER_STEP2.NAME ->
                {
                    FragmentManager.move(
                        activity!!,
                        FragmentType.REGISTER_STEP2,
                        RegisterMainFrame.viewId
                    )
                }
            }
        })
    }

    override fun onResume()
    {
        super.onResume()

        // todo 등록된 택배 운송장 번호와 비교해서 clipboard text와 같거나 운송장 번호 et에 등록 중이면 아래 로직 생략
        val text = ClipboardUtil.pasteClipboardText(SOPOApp.INSTANCE)

        val isRegister = binding.vm?.trackNumStr?.value.isNullOrEmpty()

        if (!(text.isEmpty() || !isRegister))
        {
            binding.vm?.clipboardStr?.value = text
        }
    }

}