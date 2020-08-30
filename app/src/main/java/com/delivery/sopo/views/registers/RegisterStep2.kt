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
import com.delivery.sopo.viewmodels.registesrs.RegisterViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterStep2 : Fragment()
{
    val TAG = "LOG.SOPO"
    private lateinit var binding: RegisterStep2Binding
    private val registerVm: RegisterViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.register_step2, container, false)
        binding.vm = registerVm
        binding.lifecycleOwner = this

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

}