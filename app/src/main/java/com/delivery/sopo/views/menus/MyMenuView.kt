package com.delivery.sopo.views.menus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.delivery.sopo.R
import com.delivery.sopo.databinding.MyMenuViewBinding
import com.delivery.sopo.viewmodels.menus.MyMenuViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class MyMenuView : Fragment()
{
    private val myMenuVm : MyMenuViewModel by viewModel()

    lateinit var binding: MyMenuViewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.my_menu_view, container, false)
        binding.vm = myMenuVm
        binding.lifecycleOwner = this

        setObserver()
        return binding.root
    }


    fun bindView()
    {

    }

    fun setObserver()
    {
    }
}