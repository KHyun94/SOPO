package com.delivery.sopo.models.base

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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

    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

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

    private fun getActivityResultLauncher(callback: ActivityResultCallback<ActivityResult>):ActivityResultLauncher<Intent>{
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult(), callback)
    }

    fun launchActivityResult(intent: Intent, callback: ActivityResultCallback<ActivityResult>){
        activityResultLauncher = getActivityResultLauncher(callback).apply { launch(intent) }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        activityResultLauncher?.unregister()
        activityResultLauncher = null
    }

    /**
     * 데이터 전달
     */
    protected open fun receivedData(intent: Intent){}

    /**
     * 초기 화면 세팅
     */
    protected open fun initUI(){}

    /**
     * UI 세팅 이후
     */
    protected open fun setAfterSetUI(){}

    /**
     * Observe 로직
     */
    protected open fun setObserve(){}
}