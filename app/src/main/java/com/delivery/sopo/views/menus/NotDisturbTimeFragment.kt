package com.delivery.sopo.views.menus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentNotDisturbTimeBinding
import com.delivery.sopo.databinding.MyMenuViewBinding
import com.delivery.sopo.viewmodels.menus.MyMenuViewModel
import com.delivery.sopo.viewmodels.menus.NotDisturbTimeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class NotDisturbTimeFragment : Fragment()
{
    private val notDisturbVm : NotDisturbTimeViewModel by viewModel()

    lateinit var binding: FragmentNotDisturbTimeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_not_disturb_time, container, false)
        binding.vm = notDisturbVm
        binding.lifecycleOwner = this

        setObserver()
        return binding.root
    }

    fun setObserver()
    {
    }
}