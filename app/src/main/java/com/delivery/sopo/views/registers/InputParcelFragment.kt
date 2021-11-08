package com.delivery.sopo.views.registers

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.databinding.FragmentInputParcelBinding
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.NavigatorEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.isGreaterThanOrEqual
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressListener
import com.delivery.sopo.models.ParcelRegister
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.mapper.CarrierMapper
import com.delivery.sopo.util.*
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.viewmodels.registesrs.InputParcelViewModel
import com.delivery.sopo.views.main.MainView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.system.exitProcess

/**
 * 택배 등록 1단
 */
class InputParcelFragment: BaseFragment<FragmentInputParcelBinding, InputParcelViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_input_parcel
    override val vm: InputParcelViewModel by viewModel()
    override val mainLayout: View by lazy { binding.constraintMainRegister }

    private val parentView: MainView by lazy { activity as MainView }

    private lateinit var parcelRegister: ParcelRegister
    private var returnType: Int? = null

    override fun receiveData(bundle: Bundle)
    {
        super.receiveData(bundle)

        bundle.getSerializable(RegisterMainFrame.REGISTER_INFO).let { data ->
            if(data !is ParcelRegister) return@let
            parcelRegister = data
        }

        returnType = bundle.getInt(RegisterMainFrame.RETURN_TYPE)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        onSOPOBackPressedListener = object: OnSOPOBackPressListener
        {
            override fun onBackPressedInTime()
            {
                Snackbar.make(parentView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다.", 2000).apply {
                    animationMode = Snackbar.ANIMATION_MODE_SLIDE
                }.show()
            }

            override fun onBackPressedOutTime()
            {
                ActivityCompat.finishAffinity(requireActivity())
                exitProcess(0)
            }
        }
    }

    override fun setAfterBinding()
    {
        super.setAfterBinding()

        if(::parcelRegister.isInitialized)
        {
            vm.waybillNum.postValue(parcelRegister.waybillNum)
            binding.layoutWaybillNum.hint = ""

            parcelRegister.carrier?.let {
                vm.carrier.postValue(CarrierMapper.enumToObject(it))
            }
        }

        binding.constraintMainRegister.setOnClickListener {
            it.requestFocus()
            OtherUtil.hideKeyboardSoft(requireActivity())
        }
    }

    override fun setObserve()
    {
        super.setObserve()
        parentView.currentPage.observe(this) {

            it ?: return@observe
            if(it != 0) return@observe

            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.focus.observe(this) { focus ->
            val res = TextInputUtil.changeFocus(requireContext(), focus)
            vm.validity[res.first] = res.second
        }

        vm.waybillNum.observe(this) { waybillNum ->

            if(waybillNum.isEmpty()) return@observe
            vm.clipboardText.value = ""

            if(!binding.layoutWaybillNum.hasFocus()) binding.layoutWaybillNum.requestFocus()
            if(returnType == RegisterMainFrame.REGISTER_PROCESS_RESET && ::parcelRegister.isInitialized && parcelRegister.carrier != null) return@observe

            if(!waybillNum.isGreaterThanOrEqual(9))
            {
                vm.carrier.value = null
                return@observe
            }

            vm.recommendCarrierByWaybill(waybillNum)
        }


        vm.invalidity.observe(this) { target ->
            val message = when(target.first)
            {
                InfoEnum.WAYBILL_NUMBER ->
                {
                    binding.etEmail.requestFocus()
                    "운송장번호를 확인해주세요."
                }

                else -> ""
            }

            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).apply {
                setGravity(Gravity.TOP, 0, 180)
            }.show()
        }

        vm.navigator.observe(this) { nav ->

            vm.setNavigator(null)

            val registerDTO = ParcelRegister(vm.waybillNum.value, vm.carrier.value?.carrier, null)

            when(nav)
            {
                NavigatorEnum.REGISTER_SELECT ->
                {
                    TabCode.REGISTER_SELECT.FRAGMENT = SelectCarrierFragment.newInstance(vm.waybillNum.value ?: "")
                    FragmentManager.move(parentView, TabCode.REGISTER_SELECT, RegisterMainFrame.viewId)
                }
                NavigatorEnum.REGISTER_CONFIRM ->
                {
                    TabCode.REGISTER_CONFIRM.FRAGMENT = ConfirmParcelFragment.newInstance(register = registerDTO)
                    FragmentManager.move(parentView, TabCode.REGISTER_CONFIRM, RegisterMainFrame.viewId)
                }
            }

        }
    }

    override fun onResume()
    {
        super.onResume()

        SopoLog.d(msg = "OnResume")

        // 0922 kh 추가사항 - 클립보드에 저장되어있는 운송장 번호가 로컬에 등록된 택배가 있을 때, 안띄어주는 로직 추가

        CoroutineScope(Dispatchers.Main).launch {
            val clipboardText = ClipboardUtil.pasteClipboardText(SOPOApp.INSTANCE) ?: return@launch

            val isWrite = vm.waybillNum.value.isNullOrEmpty()

            if((clipboardText.isEmpty() || !isWrite).not())
            {
                vm.clipboardText.postValue(clipboardText)
            }
        }
    }

    companion object
    {
        fun newInstance(register: ParcelRegister?, returnType: Int?): InputParcelFragment
        {
            val args = Bundle().apply {
                putSerializable(RegisterMainFrame.REGISTER_INFO, register)
                putInt(RegisterMainFrame.RETURN_TYPE, returnType ?: RegisterMainFrame.REGISTER_PROCESS_RESET)
            }

            return InputParcelFragment().apply {
                arguments = args
            }
        }
    }


}