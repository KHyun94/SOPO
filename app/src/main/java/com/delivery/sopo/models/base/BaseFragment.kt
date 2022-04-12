package com.delivery.sopo.models.base

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.delivery.sopo.BR
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.enums.NetworkStatus
import com.delivery.sopo.enums.SnackBarEnum
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressListener
import com.delivery.sopo.usecase.LogoutUseCase
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomSnackBar
import com.delivery.sopo.util.ui_util.SopoLoadingBar
import com.delivery.sopo.views.dialog.LogoutDialog
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import kotlin.system.exitProcess

abstract class BaseFragment<T: ViewDataBinding, R: BaseViewModel>: Fragment(), KoinComponent
{
    lateinit var binding: T
    abstract val layoutRes: Int
    abstract val vm: R

    abstract val mainLayout: View

    lateinit var onBackPressedCallback: OnBackPressedCallback
    lateinit var onSOPOBackPressedListener: OnSOPOBackPressListener

    /**
     * Network Status Check
     */
    private val disconnectNetworkSnackBar: CustomSnackBar by lazy {
        CustomSnackBar(mainLayout, "네트워크 오류입니다.", 600000, SnackBarEnum.ERROR)
    }

    private val reconnectNetworkSnackBar: CustomSnackBar by lazy {
        CustomSnackBar(mainLayout, "네트워크에 다시 연결되었어요.", 3000, SnackBarEnum.COMMON)
    }

    private val progressBar: SopoLoadingBar by lazy {
        SopoLoadingBar(this.requireActivity())
    }

    private var isUseCommonBackPress: Boolean = false

    fun useCommonBackPressListener(isUseCommon: Boolean)
    {
        isUseCommonBackPress = isUseCommon
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

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
        setOnBackPressedListener(owner = this)
        setObserve()
    }

    override fun onResume()
    {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onDetach()
    {
        super.onDetach()

        onBackPressedCallback.remove()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
    }

    private fun setOnBackPressedListener(owner: LifecycleOwner)
    {
        var pressedTime: Long = 0

        onBackPressedCallback = object: OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                if(isUseCommonBackPress)
                {
                    onSOPOBackPressedListener.onBackPressed()
                    return
                }

                if(System.currentTimeMillis() - pressedTime > 2000)
                {
                    pressedTime = System.currentTimeMillis()
                    onSOPOBackPressedListener.onBackPressedInTime()
                    return
                }

                onSOPOBackPressedListener.onBackPressedOutTime()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(owner, onBackPressedCallback)
    }

    private fun bindView(inflater: LayoutInflater, container: ViewGroup?): T
    {
        return DataBindingUtil.inflate<T>(inflater, layoutRes, container, false).apply {
            setVariable(BR.vm, vm)
            lifecycleOwner = viewLifecycleOwner
            executePendingBindings()
        }
    }

    protected open fun receiveData(bundle: Bundle)
    {
    }

    /**
     * 초기 화면 세팅
     */
    protected open fun setBeforeBinding()
    {
    }

    /**
     * UI 세팅 이후
     */
    protected open fun setAfterBinding()
    {
    }

    /*    */
    /**
     * Observe 로직
     */
    protected open fun setObserve()
    {
        setInnerObserve()
    }

    private fun setInnerObserve()
    {

        SOPOApp.networkStatus.observe(viewLifecycleOwner) { status ->

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

        vm.isClickEvent.observe(viewLifecycleOwner) {

            SopoLog.d("Base Click Event [data:$it]")

            if(!it) return@observe

            hideKeyboard()
        }

        vm.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if(isLoading) return@observe progressBar.show()
            else progressBar.dismiss()
        }

        vm.errorSnackBar.observe(viewLifecycleOwner) {
            val snackBar = CustomSnackBar(mainLayout, it, 3000, SnackBarEnum.ERROR)
            snackBar.show()
        }

        vm.isDuplicated.observe(viewLifecycleOwner) {
            if(!it) return@observe

            LogoutDialog(requireActivity()) {
                Handler().postDelayed(Runnable {
                    exit()
                }, 500)

            }.show(this.parentFragmentManager, "")
        }
    }

    fun exit()
    {
        ActivityCompat.finishAffinity(requireActivity())
        exitProcess(0)
    }

    fun hideKeyboard()
    {
        mainLayout.requestFocus()
        OtherUtil.hideKeyboardSoft(requireActivity())
    }
}