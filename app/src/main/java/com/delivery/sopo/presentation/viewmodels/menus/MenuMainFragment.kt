package com.delivery.sopo.presentation.viewmodels.menus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.delivery.sopo.databinding.FragmentMenuMainFrameBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.presentation.views.main.MainView

class MenuMainFragment : Fragment()
{
    lateinit var binding: FragmentMenuMainFrameBinding
    lateinit var parentView: MainView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        parentView = activity as MainView
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = FragmentMenuMainFrameBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        viewId = binding.layoutMainMenuFrame.id

        FragmentManager.add(parentView, TabCode.MY_MENU_MAIN, viewId)

        return binding.root
    }

    companion object
    {
        var viewId = 0
    }
}