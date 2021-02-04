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
import java.lang.Exception

class BindView<T : ViewDataBinding>
{
    var binding : T? = null
    var activity : FragmentActivity? = null
    @LayoutRes
    var resId : Int = 0
    var inflater : LayoutInflater? = null
    var container : ViewGroup? = null
    var vm : ViewModel? = null
    /** 0 : Activity 1 : Fragment */
    var TYPE = 0

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

    fun bindView() : T?
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
            else -> return null
        }

        binding!!.lifecycleOwner = activity
        binding!!.executePendingBindings()

        return binding
    }

    companion object{
        const val BIND_TYPE_ACTIVITY = 0
        const val BIND_TYPE_FRAGMENT = 1
    }

}