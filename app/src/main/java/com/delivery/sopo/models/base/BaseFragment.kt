package com.delivery.sopo.models.base

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

abstract class BaseFragment<T: ViewDataBinding, R: ViewModel>: Fragment()
{

    lateinit var binding: T
    abstract val layoutRes: Int
    abstract val vm: R

    abstract val mainLayout: View

    lateinit var onBackPressedCallback: OnBackPressedCallback

    lateinit var onSOPOBackPressedListener: OnSOPOBackPressListener

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        var pressedTime: Long = 0

        onBackPressedCallback = object: OnBackPressedCallback(true)
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

        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        arguments?.let { bundle -> receiveData(bundle) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        setBeforeBinding()
        binding = bindView(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        setAfterBinding()
        setObserve()
    }

    private fun bindView(inflater: LayoutInflater, container: ViewGroup?): T
    {
        return DataBindingUtil.inflate<T>(inflater, layoutRes, container, false).apply {
            setVariable(BR.vm, vm)
            lifecycleOwner = this@BaseFragment
        }
    }

    override fun onDetach()
    {
        super.onDetach()

        onBackPressedCallback.remove()
    }

    protected open fun receiveData(bundle: Bundle){
    }

    /**
     * 초기 화면 세팅
     */
    protected open fun setBeforeBinding() {}

    /**
     * UI 세팅 이후
     */
    protected open fun setAfterBinding() {}

    /*    */
    /**
     * Observe 로직
     */
    protected open fun setObserve() {}
}