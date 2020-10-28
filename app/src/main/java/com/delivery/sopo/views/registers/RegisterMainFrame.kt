package com.delivery.sopo.views.registers

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.delivery.sopo.R
import com.delivery.sopo.databinding.RegisterMainFrameBinding
import com.delivery.sopo.enums.FragmentTypeEnum
import com.delivery.sopo.interfaces.listener.OnMainBackPressListener
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.viewmodels.registesrs.RegisterStep1ViewModel
import com.delivery.sopo.views.main.MainView
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

        FragmentManager.move(this.requireActivity(),FragmentTypeEnum.REGISTER_STEP1, viewId)

        return binding.root
    }

//    override fun onResume()
//    {
//        super.onResume()
//        Log.d("!!!!", "RegisterMainFrame onResume() !!!!!!!!!!!!!!")
//        parentView.setOnBackPressListener(object : OnMainBackPressListener
//        {
//            override fun onBackPressed()
//            {
//                Log.d("!!!!!!!!", "OnBackPressed RegisterStep")
//                FragmentManager.remove(activity!!)
//            }
//        })
//    }

    companion object{
        var viewId : Int = 0
    }
}