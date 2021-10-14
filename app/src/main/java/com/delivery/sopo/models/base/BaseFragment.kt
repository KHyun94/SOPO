package com.delivery.sopo.models.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.delivery.sopo.BR
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressListener
import com.delivery.sopo.util.SopoLog

abstract class BaseFragment<T: ViewDataBinding, R: ViewModel>: Fragment() {

    lateinit var binding: T

    abstract val layoutRes: Int
    abstract val vm: R

    private lateinit var callback: OnBackPressedCallback

    lateinit var onSOPOBackPressedListener: OnSOPOBackPressListener

    override fun onAttach(context: Context)
    {
        super.onAttach(context)

        var pressedTime: Long = 0

        callback = object: OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                if(System.currentTimeMillis() - pressedTime > 2000)
                {
                    pressedTime = System.currentTimeMillis()
                    onSOPOBackPressedListener.onBackPressedInTime()
                    return
                }

                onSOPOBackPressedListener.onBackPressedOutTime()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = bindView(inflater, container)



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            this.lifecycleOwner = lifecycleOwner
            setVariable(BR.vm, vm)
        }

        initUI()
        setObserve()
        setAfterSetUI()
    }

    private fun bindView(inflater: LayoutInflater, container: ViewGroup?) : T
    {
        return DataBindingUtil.inflate<T>(inflater, layoutRes, container, false)
    }

    override fun onDetach()
    {
        super.onDetach()

        callback.remove()
    }

    /**
     * 초기 화면 세팅
     */
    protected open fun initUI(){
        SopoLog.d("base fragment - initUI Call1")
    }

    /**
     * UI 세팅 이후
     */
    abstract fun setAfterSetUI()

/*    *//**
     * Observe 로직
     */
    protected open fun setObserve() {}
}