package com.delivery.sopo.models.base

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.delivery.sopo.BR
import com.delivery.sopo.SOPOApplication
import com.delivery.sopo.domain.usecase.user.token.LogoutUseCase
import com.delivery.sopo.enums.NetworkStatus
import com.delivery.sopo.enums.SnackBarEnum
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressListener
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomSnackBar
import com.delivery.sopo.util.ui_util.SopoLoadingBar
import com.delivery.sopo.presentation.views.dialog.LogoutDialog
import com.delivery.sopo.util.KeyboardVisibilityUtil
import com.delivery.sopo.util.ui_util.BottomNotificationBar
import com.orhanobut.logger.Logger
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.system.exitProcess

abstract class BaseFragment<T: ViewDataBinding, R: BaseViewModel>: Fragment(), KoinComponent
{
    lateinit var binding: T
    abstract val layoutRes: Int
    abstract val vm: R

    abstract val mainLayout: View

    lateinit var onBackPressedCallback: OnBackPressedCallback
    lateinit var onSOPOBackPressedListener: OnSOPOBackPressListener

    protected lateinit var snackBar: BottomNotificationBar
    private lateinit var keyboardVisibilityUtil: KeyboardVisibilityUtil

    protected var toast: Toast? = null

    private val progressBar: SopoLoadingBar by lazy {
        SopoLoadingBar(this.requireActivity())
    }

    private var isUseCommonBackPress: Boolean = false

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT){
        requireActivity().runOnUiThread {
            if(toast != null) toast?.cancel()
            toast = Toast.makeText(requireContext(), message, duration)
            toast?.show()
        }
    }

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

    override fun onDestroy()
    {
        super.onDestroy()
        toast?.cancel()
        if(::keyboardVisibilityUtil.isInitialized) keyboardVisibilityUtil.detachKeyboardListeners()
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

        activity?.onBackPressedDispatcher?.addCallback(owner, onBackPressedCallback)
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
        keyboardVisibilityUtil = KeyboardVisibilityUtil(requireActivity().window, { onShowKeyboard() }, { onHideKeyboard() })
    }


    protected open fun onShowKeyboard(){ }

    protected open fun onHideKeyboard(){ }

    /**
     * UI 세팅 이후
     */
    protected open fun setAfterBinding() { }

    /**
     * Observe 로직
     */
    protected open fun setObserve()
    {
        setInnerObserve()
    }

    private fun setInnerObserve()
    {
        vm.snackBar.observe(viewLifecycleOwner) {

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
            Logger.e("SnackBar $it")
            CustomSnackBar.make(mainLayout, it, Unit, SnackBarEnum.ERROR).show()
        }

        vm.toast.observe(viewLifecycleOwner) { toast ->
            showToast(toast, Toast.LENGTH_SHORT)
        }

        vm.isDuplicated.observe(viewLifecycleOwner) {
            if(!it) return@observe

            LogoutDialog(requireActivity()) {
                logoutUseCase()
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    exit()
                }, 1000)

            }.show(this.parentFragmentManager, "")
        }
    }

    val logoutUseCase: LogoutUseCase by inject()

    fun exit()
    {
        ActivityCompat.finishAffinity(requireActivity())
        exitProcess(0)
    }

    fun showKeyboard(target: EditText)
    {
        target.requestFocus()
        OtherUtil.showKeyboardSoft(requireActivity(), target)
    }

    fun hideKeyboard()
    {
        mainLayout.requestFocus()
        OtherUtil.hideKeyboardSoft(requireActivity())
    }
}