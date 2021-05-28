package com.delivery.sopo.abstracts

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel

abstract class BasicFragmentView<T : ViewDataBinding, V: ViewModel> : Fragment()
{
    lateinit var parentActivity: Activity
    lateinit var binding: T

    protected lateinit var vm: V

    abstract var layoutId: Int
    abstract var bindingVariable: Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        binding.lifecycleOwner = this
        binding.setVariable(bindingVariable, vm)
        setObserver()
        return binding.root
    }

    abstract fun setObserver()
}