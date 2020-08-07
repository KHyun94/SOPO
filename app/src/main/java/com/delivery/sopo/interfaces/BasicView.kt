package com.delivery.sopo.interfaces

import android.app.Activity
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.delivery.sopo.R

abstract class BasicView<T:ViewDataBinding>(@LayoutRes val layoutRes:Int) : AppCompatActivity() {
    var TAG = "LOG.SOPO"
    lateinit var parentActivity : Activity
    lateinit var binding : T

    abstract fun bindView()
    abstract fun setObserver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<T>(this, layoutRes)
        binding.lifecycleOwner = this

        bindView()
        setObserver()
    }
}