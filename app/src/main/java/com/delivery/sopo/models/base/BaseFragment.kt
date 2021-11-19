package com.delivery.sopo.models.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.delivery.sopo.BR
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.enums.NetworkStatus
import com.delivery.sopo.enums.SnackBarEnum
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressListener
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.util.ui_util.CustomSnackBar

abstract class BaseFragment<T: ViewDataBinding, R: BaseViewModel>: Fragment()
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

    private val progressBar: CustomProgressBar by lazy {
        CustomProgressBar(this.requireActivity())
    }

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
    protected open fun setObserve(){
        setInnerObserve()
    }

    private fun setInnerObserve(){

        SOPOApp.networkStatus.observe(viewLifecycleOwner){ status ->

            SopoLog.d("status [status:$status]")

            if(vm.isCheckNetwork.value != true) return@observe

            when(status){

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

            val a = mainLayout.requestFocus()
            SopoLog.d("request Focus [${a}]")
            OtherUtil.hideKeyboardSoft(requireActivity())
        }

        vm.isLoading.observe(viewLifecycleOwner){ isLoading ->
            if(isLoading) return@observe progressBar.onStartLoading()
            else progressBar.onStopLoading()
        }

        vm.errorSnackBar.observe(viewLifecycleOwner){
            val snackBar = CustomSnackBar(mainLayout, it, 3000, SnackBarEnum.ERROR)
            snackBar.show()
        }
    }
}