package com.delivery.sopo.views.registers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.delivery.sopo.R
import com.delivery.sopo.databinding.RegisterDeliveryCoBinding
import com.delivery.sopo.databinding.RegisterTrackNumBinding
import com.delivery.sopo.viewmodels.registesrs.RegisterViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterDeliveryCo : Fragment()
{
    private lateinit var binding : RegisterDeliveryCoBinding
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
        binding = DataBindingUtil.inflate(inflater, R.layout.register_delivery_co, container, false)
        binding.vm = registerVm
        binding.lifecycleOwner = this

        return binding.root
    }
}