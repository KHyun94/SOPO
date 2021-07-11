package com.delivery.sopo.util

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner

object BindUtil
{
    fun <T : ViewDataBinding> bindView(activity: FragmentActivity,  @LayoutRes layoutId : Int) : T
    {
        return DataBindingUtil.setContentView<T>(activity,layoutId).apply{
            this.lifecycleOwner = lifecycleOwner
            executePendingBindings()
        }
    }

    fun <T : ViewDataBinding> bindView(lifecycleOwner: LifecycleOwner, inflater : LayoutInflater, @LayoutRes layoutId : Int, container : ViewGroup?) : T
    {
        return DataBindingUtil.inflate<T>(inflater, layoutId, container, false).apply{
            this.lifecycleOwner = lifecycleOwner
            executePendingBindings()
        }
    }
}