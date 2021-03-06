package com.delivery.sopo.views.menus

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.delivery.sopo.R
import com.delivery.sopo.databinding.MenuMainFrameBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.viewmodels.menus.MenuMainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MenuMainFrame : Fragment()
{
    private lateinit var binding: MenuMainFrameBinding
    val vm : MenuMainViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.menu_main_frame, container, true)
        binding.vm = vm
        binding.lifecycleOwner = this
        viewId = binding.menuMainFrame.id
        FragmentManager.move(this.requireActivity(), TabCode.MY_MENU, viewId)

        return binding.root
    }

    companion object{
        var viewId : Int = 0
    }
}