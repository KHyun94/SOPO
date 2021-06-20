package com.delivery.sopo.views.menus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.delivery.sopo.databinding.AppInfoViewBinding
import com.delivery.sopo.viewmodels.menus.AppInfoViewModel
import com.delivery.sopo.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class AppInfoFragment : Fragment()
{

    private val appInfoVm: AppInfoViewModel by viewModel()
    private lateinit var binding: AppInfoViewBinding

    private lateinit var parentView: MainView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        parentView = activity as MainView
        binding = AppInfoViewBinding.inflate(inflater, container, false)
        viewBinding()
        setObserver()



        return binding.root
    }

    private fun viewBinding()
    {
        binding.vm = appInfoVm
        binding.lifecycleOwner = this
        binding.executePendingBindings() // 즉 바인딩
    }

    fun setObserver()
    {
        
    }


}