package com.delivery.sopo.views.registers

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.databinding.RegisterStep2Binding
import com.delivery.sopo.enums.FragmentType
import com.delivery.sopo.util.ui_util.FragmentManager
import com.delivery.sopo.viewmodels.registesrs.RegisterStep1ViewModel
import com.delivery.sopo.viewmodels.registesrs.RegisterStep2ViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterStep2 : Fragment()
{
    val TAG = "LOG.SOPO"
    private lateinit var binding: RegisterStep2Binding
    private val registerStep2Vm: RegisterStep2ViewModel by viewModel()

    private var waybilNum: String? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        if (arguments != null)
        {
            waybilNum = arguments!!.getString("waybilNum") ?: ""

            Log.d(TAG, "Way Back Home => ${waybilNum}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.register_step2, container, false)

        registerStep2Vm.trackNumStr.value = waybilNum

        binding.vm = registerStep2Vm
        binding.lifecycleOwner = this


        Log.d(TAG, "Way Back Home 2 => ${binding.vm!!.trackNumStr.value}")
//        if (waybilNum != null && waybilNum!!.isNotEmpty())
//        {
//            binding.vm!!.trackNumStr.value = waybilNum
//        }

        setObserve()

        Log.d(TAG, "vm =>> ${binding.vm?.trackNumStr?.value}")

        return binding.root
    }
//(activity as RegisterMainFrame).childFragmentManager
    fun setObserve()
    {
        binding.vm?.moveFragment?.observe(this, Observer {

            when (it)
            {
                FragmentType.REGISTER_STEP1.NAME ->
                {

                }
                FragmentType.REGISTER_STEP2.NAME ->
                {
                    FragmentManager.remove(activity = activity!!)
                    binding.vm?.moveFragment?.value = ""
                }
            }
        })
    }

    companion object
    {
        fun newInstance(waybilNum: String?): RegisterStep2
        {
            val registerStep2 = RegisterStep2()

            val args = Bundle()
            args.putString("waybilNum", waybilNum)

            registerStep2.arguments = args
            return registerStep2
        }
    }

}