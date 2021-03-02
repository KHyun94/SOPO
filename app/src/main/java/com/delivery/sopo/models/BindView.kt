package com.delivery.sopo.models

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.delivery.sopo.BR
import com.delivery.sopo.util.SopoLog
import java.lang.Exception

class BindView<T : ViewDataBinding>
{
    private lateinit var binding : T
    var activity : FragmentActivity? = null
    @LayoutRes
    var resId : Int = 0
    var inflater : LayoutInflater? = null
    var container : ViewGroup? = null
    var vm : ViewModel? = null
    /** 0 : Activity 1 : Fragment */
    private var TYPE : Int

    constructor(activity : FragmentActivity, @LayoutRes resId : Int)
    {
        this.activity = activity
        this.resId = resId
        TYPE = BIND_TYPE_ACTIVITY
    }

    constructor(activity : FragmentActivity, inflater : LayoutInflater, container : ViewGroup?, @LayoutRes resId : Int)
    {
        this.activity = activity
        this.inflater = inflater
        this.container = container
        this.resId = resId

        TYPE = BIND_TYPE_FRAGMENT
    }

    fun bindView() : T
    {
        binding = when(TYPE)
        {
            BIND_TYPE_ACTIVITY ->
            {
                DataBindingUtil.setContentView<T>(activity!!.parent, resId)
            }
            BIND_TYPE_FRAGMENT ->
            {
                DataBindingUtil.inflate(inflater!!, resId, container, false)
            }
            else -> DataBindingUtil.setContentView<T>(activity!!.parent, resId)
        }

        binding.lifecycleOwner = activity

        return binding
    }

    fun setExecutePendingBindings()
    {
        try
        {
            binding.executePendingBindings()
        }
        catch (e:Exception)
        {
            throw e
        }
    }

    fun<T:ViewModel> setViewModel(vm: T)
    {
        try
        {
            binding.setVariable(BR.vm, vm)
        }
        catch (e:Exception)
        {
            throw e
        }
    }

    companion object{
        const val BIND_TYPE_ACTIVITY = 0
        const val BIND_TYPE_FRAGMENT = 1
    }
}