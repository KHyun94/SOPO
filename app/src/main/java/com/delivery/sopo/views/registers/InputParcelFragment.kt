package com.delivery.sopo.views.registers

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentInputParcelBinding
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.NavigatorEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.isGreaterThanOrEqual
import com.delivery.sopo.interfaces.OnPageSelectListener
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.mapper.CarrierMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.*
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.viewmodels.registesrs.InputParcelViewModel
import com.delivery.sopo.views.main.MainView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 택배 등록 1단
 */
class InputParcelFragment: BaseFragment<FragmentInputParcelBinding, InputParcelViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_input_parcel
    override val vm: InputParcelViewModel by viewModel()
    override val mainLayout: View by lazy { binding.constraintMainRegister }

    private val parentView: MainView by lazy { activity as MainView }

    private lateinit var onPageSelectListener: OnPageSelectListener

    private fun setOnMainBridgeListener(context: Context)
    {
        onPageSelectListener = context as OnPageSelectListener
    }

    private lateinit var parcelRegister: Parcel.Register
    private var returnType: Int? = null

    override fun receiveData(bundle: Bundle)
    {
        super.receiveData(bundle)

        bundle.getSerializable(RegisterMainFragment.REGISTER_INFO).let { data ->
            if(data !is Parcel.Register) return@let
            parcelRegister = data
        }

        returnType = bundle.getInt(RegisterMainFragment.RETURN_TYPE)
    }

    override fun setBeforeBinding()
    {
        super.setBeforeBinding()

        onSOPOBackPressedListener = object: OnSOPOBackPressEvent()
        {
            override fun onBackPressedInTime()
            {
                Snackbar.make(parentView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다.", 2000).apply { animationMode = Snackbar.ANIMATION_MODE_SLIDE }.show()
            }

            override fun onBackPressedOutTime()
            {
                exit()
            }
        }

        setOnMainBridgeListener(requireContext())

        if(returnType == RegisterMainFragment.REGISTER_PROCESS_SUCCESS)
        {
            onPageSelectListener.onMoveToPage(1)
        }
    }

    override fun setAfterBinding()
    {
        super.setAfterBinding()

        if(::parcelRegister.isInitialized)
        {
            vm.waybillNum.postValue(parcelRegister.waybillNum)
            binding.layoutWaybillNum.hint = ""

            parcelRegister.carrier?.let { vm.carrier.postValue(CarrierMapper.enumToObject(it)) }
        }

        binding.constraintMainRegister.setOnClickListener {
            it.requestFocus()
            OtherUtil.hideKeyboardSoft(requireActivity())
        }
    }

    override fun setObserve()
    {
        super.setObserve()

        activity?:return

        parentView.currentPage.observe(this) {
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
            if(returnType == RegisterMainFragment.REGISTER_PROCESS_RESET && ::parcelRegister.isInitialized && parcelRegister.carrier != null) return@observe

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

            val registerDTO = Parcel.Register(vm.waybillNum.value, vm.carrier.value?.carrier, null)

            when(nav)
            {
                NavigatorEnum.REGISTER_SELECT ->
                {
                    TabCode.REGISTER_SELECT.FRAGMENT = SelectCarrierFragment.newInstance(vm.waybillNum.value ?: "")
                    FragmentManager.move(parentView, TabCode.REGISTER_SELECT, RegisterMainFragment.viewId)
                }
                NavigatorEnum.REGISTER_CONFIRM ->
                {
                    TabCode.REGISTER_CONFIRM.FRAGMENT = ConfirmParcelFragment.newInstance(register = registerDTO, beforeStep = 0)
                    FragmentManager.move(parentView, TabCode.REGISTER_CONFIRM, RegisterMainFragment.viewId)
                }
            }

        }
    }

    override fun onResume()
    {
        super.onResume()

        CoroutineScope(Dispatchers.Default).launch {
            val clipBoardData = ClipboardUtil.pasteClipboardText(context = requireContext())
            vm.clipboardText.postValue(clipBoardData)
        }
    }

    companion object
    {
        fun newInstance(parcelRegister: Parcel.Register?, returnType: Int?): InputParcelFragment
        {
            val args = Bundle().apply {
                putSerializable(RegisterMainFragment.REGISTER_INFO, parcelRegister)
                putInt(RegisterMainFragment.RETURN_TYPE, returnType ?: RegisterMainFragment.REGISTER_PROCESS_RESET)
            }

            return InputParcelFragment().apply {
                arguments = args
            }
        }
    }



}