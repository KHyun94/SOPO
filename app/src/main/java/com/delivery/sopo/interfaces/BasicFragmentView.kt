package com.delivery.sopo.interfaces

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BasicFragmentView<T : ViewDataBinding>(@LayoutRes val layoutRes: Int) : Fragment()
{
    lateinit var parentActivity: Activity
    lateinit var binding: T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        binding = DataBindingUtil.inflate(inflater, layoutRes, container, false)
        binding.lifecycleOwner = this
        bindView()
        setObserver()
        return binding.root
    }

    abstract fun bindView()
    abstract fun setObserver()
}