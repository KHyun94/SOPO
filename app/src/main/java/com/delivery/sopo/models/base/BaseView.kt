package com.delivery.sopo.models.base

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import com.delivery.sopo.BR
import com.delivery.sopo.SOPOApplication
import com.delivery.sopo.enums.NetworkStatus
import com.delivery.sopo.enums.SnackBarEnum
import com.delivery.sopo.enums.SnackBarType
import com.delivery.sopo.interfaces.OnSnackBarController
import com.delivery.sopo.util.NetworkStatusMonitor
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.BottomNotificationBar
import com.delivery.sopo.util.ui_util.CustomSnackBar
import com.delivery.sopo.util.ui_util.SopoLoadingBar
import kotlin.system.exitProcess

interface OnActivityResultCallbackListener
{
    fun callback(activityResult: ActivityResult)
}

abstract class BaseView<T: ViewDataBinding, R: BaseViewModel>: AppCompatActivity(), OnSnackBarController
{
    lateinit var binding: T
    abstract val layoutRes: Int
    abstract val vm: R

    abstract val mainLayout: View

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var onActivityResultCallbackListener: OnActivityResultCallbackListener
    lateinit var networkStatusMonitor: NetworkStatusMonitor

    private lateinit var snackBar: BottomNotificationBar

    fun setOnActivityResultCallbackListener(listener: OnActivityResultCallbackListener)
    {
        this.onActivityResultCallbackListener = listener
    }

    val loadingBar: SopoLoadingBar by lazy {
        SopoLoadingBar(this)
    }

    override fun setSnackBar(bottomNotificationBar: BottomNotificationBar)
    {
        snackBar = bottomNotificationBar
    }

    override fun getSnackBar(): BottomNotificationBar
    {
        return snackBar
    }

    override fun onMake(snackBarType: SnackBarType)
    {
        if(!::snackBar.isInitialized) throw Exception()
        snackBar.make(snackBarType)
    }

    override fun onShow(hasDelay: Boolean)
    {
        if(!::snackBar.isInitialized) return
        snackBar.show()
    }

    override fun onDismiss()
    {
        if(!::snackBar.isInitialized) return
        snackBar.dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        receivedData(intent = intent)
        onBeforeBinding()

        binding = bindView(this)

        vm.setCheckNetwork(true)

        networkStatusMonitor = NetworkStatusMonitor(this)
        networkStatusMonitor.enable()
        networkStatusMonitor.initNetworkCheck()

        activityResultLauncher = getActivityResultLauncher { onActivityResultCallbackListener.callback(it) }

        onAfterBinding()
        setObserve()
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

    protected open fun onDeactivateNetwork()
    {
        vm.setCheckNetwork(true)
    }

    protected open fun onActivateNetwork()
    {
        vm.setCheckNetwork(false)
    }

    private fun setInnerObserve()
    {
        SOPOApplication.networkStatus.observe(this) { status ->

            SopoLog.d("status [status:$status]")

            if(vm.currentNetworkState == NetworkStatus.DEFAULT)
            {
                vm.currentNetworkState = status
                return@observe
            }

            when(status)
            {
                NetworkStatus.WIFI, NetworkStatus.CELLULAR ->
                {
                    if(vm.currentNetworkState != NetworkStatus.NOT_CONNECT) return@observe
                    onActivateNetwork()
                }
                NetworkStatus.NOT_CONNECT ->
                {
                    onDeactivateNetwork()
                }
            }

            vm.currentNetworkState = status
        }

        vm.isClickEvent.observe(this) {
            SopoLog.d("Base Click Event [data:$it]")
            if(!it) return@observe
            hideKeyboard()
        }

        vm.isLoading.observe(this) { isLoading ->
            if(isLoading)
            {
                loadingBar.show()
            }
            else
            {
                loadingBar.dismiss()
            }
        }

        vm.errorSnackBar.observe(this) {

            CustomSnackBar.make(view = mainLayout, content = it, data = Unit, type = SnackBarEnum.ERROR, clickListener = vm.onSnackClickListener)
                .show()
        }
    }

    fun showKeyboard(target: EditText)
    {
        target.requestFocus()
        OtherUtil.showKeyboardSoft(this, target)
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
    }

    fun exit()
    {
        ActivityCompat.finishAffinity(this)
        exitProcess(0)
    }
}

