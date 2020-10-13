package com.delivery.sopo.views.registers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.delivery.sopo.R
import com.delivery.sopo.databinding.RegisterMainFrameBinding
import com.delivery.sopo.enums.FragmentTypeEnum
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.viewmodels.registesrs.RegisterStep1ViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterMainFrame : Fragment()
{
    private lateinit var binding: RegisterMainFrameBinding
    private val registerStep1Vm: RegisterStep1ViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.register_main_frame, container, true)
        binding.vm = registerStep1Vm
        binding.lifecycleOwner = this

        viewId = binding.frameRegister.id

//        FragmentType.REGISTER_STEP1.FRAGMENT = RegisterStep1.newInstance("11111111111", "CJ대한통운")

        FragmentManager.move(this.activity!!,FragmentTypeEnum.REGISTER_STEP1, viewId)

        return binding.root
    }

    companion object{
        var viewId : Int = 0
    }
}