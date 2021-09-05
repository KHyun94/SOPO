package com.delivery.sopo.models.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.delivery.sopo.BR

abstract class BaseView<T: ViewDataBinding, R: ViewModel>: AppCompatActivity() {

    lateinit var binding: T

    abstract val layoutRes: Int
    abstract val vm: R

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindView(this)

        receivedData(intent = intent)
        initUI()
        setAfterSetUI()
        setObserve()
    }

    private fun bindView(activity: FragmentActivity) : T
    {
        return DataBindingUtil.setContentView<T>(activity, layoutRes).apply{
            this.lifecycleOwner = lifecycleOwner
            this.setVariable(BR.vm, vm)
            executePendingBindings()
        }
    }

    /**
     * 데이터 전달
     */
    abstract fun receivedData(intent: Intent)

    /**
     * 초기 화면 세팅
     */
    abstract fun initUI()

    /**
     * UI 세팅 이후
     */
    abstract fun setAfterSetUI()

    /**
     * Observe 로직
     */
    abstract fun setObserve()
}