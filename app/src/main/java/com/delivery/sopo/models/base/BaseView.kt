package com.delivery.sopo.models.base

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.delivery.sopo.BR
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.enums.NetworkStatus
import com.delivery.sopo.enums.SnackBarEnum
import com.delivery.sopo.util.NetworkStatusMonitor
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomSnackBar
import com.delivery.sopo.util.ui_util.SopoLoadingBar

interface OnActivityResultCallbackListener{
    fun callback(activityResult: ActivityResult)
}

abstract class BaseView<T: ViewDataBinding, R: BaseViewModel>: AppCompatActivity()
{
    lateinit var binding: T
    abstract val layoutRes: Int
    abstract val vm: R

    abstract val mainLayout: View

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var onActivityResultCallbackListener: OnActivityResultCallbackListener
    lateinit var networkStatusMonitor: NetworkStatusMonitor

    fun setOnActivityResultCallbackListener(listener: OnActivityResultCallbackListener){
        this.onActivityResultCallbackListener = listener
    }

    /**
     * Network Status Check
     */
    private val disconnectNetworkSnackBar: CustomSnackBar by lazy {
        CustomSnackBar(mainLayout, "네트워크 오류입니다.", 600000, SnackBarEnum.ERROR)
    }

    private val reconnectNetworkSnackBar: CustomSnackBar by lazy {
        CustomSnackBar(mainLayout, "네트워크에 다시 연결되었어요.", 3000, SnackBarEnum.COMMON)
    }

    val loadingBar: SopoLoadingBar by lazy {
        SopoLoadingBar(this)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        receivedData(intent = intent)
        onBeforeBinding()

        binding = bindView(this)

        networkStatusMonitor = NetworkStatusMonitor(this)
        networkStatusMonitor.enable()
        networkStatusMonitor.initNetworkCheck()

        onAfterBinding()
        setObserve()

        activityResultLauncher = getActivityResultLauncher { onActivityResultCallbackListener.callback(it) }
    }


    private fun bindView(activity: FragmentActivity): T
    {
        return DataBindingUtil.setContentView<T>(activity, layoutRes).apply {
            this.lifecycleOwner = this@BaseView
            this.setVariable(BR.vm, vm)
            executePendingBindings()
        }
    }

    private fun getActivityResultLauncher(callback: ActivityResultCallback<ActivityResult>): ActivityResultLauncher<Intent>
    {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult(), callback)
    }

    fun launchActivityResult(intent: Intent)
    {
        activityResultLauncher.launch(intent)
    }


    /**
     * 데이터 전달
     */
    protected open fun receivedData(intent: Intent)
    {
    }

    /**
     * 초기 화면 세팅
     */
    protected open fun onBeforeBinding()
    {
    }

    /**
     * UI 세팅 이후
     */
    protected open fun onAfterBinding()
    {
    }

    /**
     * Observe 로직
     */
    protected open fun setObserve()
    {
        setInnerObserve()
    }

    private fun setInnerObserve()
    {

        SOPOApp.networkStatus.observe(this) { status ->

            SopoLog.d("status [status:$status]")

            if(vm.isCheckNetwork.value != true) return@observe

            when(status)
            {

                NetworkStatus.WIFI, NetworkStatus.CELLULAR ->
                {
                    disconnectNetworkSnackBar.dismiss()
                    reconnectNetworkSnackBar.show()
                    vm.stopToCheckNetworkStatus()
                }
                NetworkStatus.NOT_CONNECT ->
                {
                    disconnectNetworkSnackBar.show()
                }
            }
        }

        vm.isClickEvent.observe(this) {
            SopoLog.d("Base Click Event [data:$it]")
            if(!it) return@observe
            hideKeyboard()
        }

        vm.isLoading.observe(this) { isLoading ->
            if(isLoading) return@observe loadingBar.show()
            else loadingBar.dismiss()
        }

        vm.errorSnackBar.observe(this) {
            val snackBar = CustomSnackBar(mainLayout, it, 3000, SnackBarEnum.ERROR)
            snackBar.show()
        }
    }

    fun hideKeyboard()
    {
        mainLayout.requestFocus()
        OtherUtil.hideKeyboardSoft(this)
    }

    override fun onDestroy()
    {
        super.onDestroy()

        networkStatusMonitor.disable()

        activityResultLauncher.unregister()
        //        activityResultLauncher = null

    }
}
