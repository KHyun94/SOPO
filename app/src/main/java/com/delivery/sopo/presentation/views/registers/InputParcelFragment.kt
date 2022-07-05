package com.delivery.sopo.presentation.views.registers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.delivery.sopo.R
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.databinding.FragmentInputParcelBinding
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.SnackBarType
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.isGreaterThanOrEqual
import com.delivery.sopo.interfaces.OnPageSelectListener
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.mapper.CarrierMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.presentation.consts.IntentConst
import com.delivery.sopo.presentation.models.enums.ReturnType
import com.delivery.sopo.presentation.viewmodels.registesrs.InputParcelViewModel
import com.delivery.sopo.presentation.views.main.MainView
import com.delivery.sopo.util.*
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class InputParcelFragment: BaseFragment<FragmentInputParcelBinding, InputParcelViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_input_parcel
    override val vm: InputParcelViewModel by viewModel()
    override val mainLayout: View by lazy { binding.constraintMainRegister }

    private val motherView: MainView by lazy { activity as MainView }

    private lateinit var onPageSelectListener: OnPageSelectListener

    private lateinit var parcel: Parcel.Common

    private fun setOnMotherViewBridgeListener(context: Context)
    {
        onPageSelectListener = context as OnPageSelectListener
    }

    private lateinit var registerInfo: Parcel.Register
    private var returnType: ReturnType? = null

    override fun receiveData(bundle: Bundle)
    {
        super.receiveData(bundle)

        bundle.getSerializable(RegisterMainFragment.REGISTER_INFO).let { data ->
            if(data !is Parcel.Register) return@let
            registerInfo = data
        }

        bundle.getSerializable(RegisterMainFragment.PARCEL).let { data ->
            if(data !is Parcel.Common) return@let
            parcel = data
        }

        returnType = bundle.getSerializable(RegisterMainFragment.RETURN_TYPE) as ReturnType?
    }

    override fun setBeforeBinding()
    {
        super.setBeforeBinding()

        onSOPOBackPressedListener = object: OnSOPOBackPressEvent()
        {
            override fun onBackPressedInTime()
            {
                Snackbar.make(motherView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다.", 2000)
                    .apply { animationMode = Snackbar.ANIMATION_MODE_SLIDE }
                    .show()
            }

            override fun onBackPressedOutTime()
            {
                exit()
            }
        }

        setOnMotherViewBridgeListener(requireContext())
    }

    fun moveInquiryTap()
    {
        if(!::parcel.isInitialized) return
        val intent = Intent()

        if(parcel.isDelivered())
        {
            val date = parcel.getDeliveredAlarm()

            motherView.onMake(SnackBarType.Common("${date}에 배송완료된 택배네요.", 3000))



            (mainLayout) { date ->
                motherView.onSetCurrentPage(1)

                intent.action = IntentConst.Action.REGISTERED_COMPLETED_PARCEL
                intent.putExtra(IntentConst.Extra.REGISTERED_DATE, date)
                motherView.sendBroadcast(intent)
            }
        }
        else
        {
            parcel.makeOtherAlarm(mainLayout) {
                motherView.onSetCurrentPage(1)

                intent.action = IntentConst.Action.REGISTERED_ONGOING_PARCEL
                motherView.sendBroadcast(intent)
            }
        }
    }

    override fun setAfterBinding()
    {
        super.setAfterBinding()

        moveInquiryTap()

        binding.constraintMainRegister.setOnClickListener {
            it.requestFocus()
            OtherUtil.hideKeyboardSoft(requireActivity())
        }

        if(!::registerInfo.isInitialized) return

        if(registerInfo.waybillNum != null && registerInfo.waybillNum?.length ?: 0 > 0)
        {
            vm.waybillNum.postValue(registerInfo.waybillNum)
            binding.layoutWaybillNum.hint = ""
        }
        registerInfo.carrier?.let { vm.carrier.postValue(CarrierMapper.enumToObject(it)) }
    }

    override fun setObserve()
    {
        super.setObserve()

        activity ?: return

        motherView.getCurrentPage().observe(this) {
            if(it != TabCode.firstTab) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.focus.observe(this) { focus ->
            val res = TextInputUtil.changeFocus(requireContext(), focus)
            vm.validity[res.first] = res.second
        }

        vm.waybillNum.observe(this) { waybillNum ->

            SopoLog.d("WaybillNum :: $waybillNum")

            if(waybillNum.isEmpty()) return@observe
            vm.clipboardText.value = ""

            if(!binding.layoutWaybillNum.hasFocus()) binding.layoutWaybillNum.requestFocus()
            if(returnType == ReturnType.INIT_PARCEL && ::registerInfo.isInitialized && registerInfo.carrier != null) return@observe

            if(!waybillNum.isGreaterThanOrEqual(9))
            {
                vm.carrier.value = null
                return@observe
            }

            vm.recommendCarrier(waybillNum)
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

            val registerParcel = Parcel.Register(vm.waybillNum.value, vm.carrier.value?.carrier, null)

            when(nav)
            {
                NavigatorConst.REGISTER_SELECT_CARRIER ->
                {
                    TabCode.REGISTER_SELECT.FRAGMENT = SelectCarrierFragment.newInstance(vm.waybillNum.value ?: "")
                    FragmentManager.move(motherView, TabCode.REGISTER_SELECT, RegisterMainFragment.viewId)
                }
                NavigatorConst.REGISTER_CONFIRM_PARCEL ->
                {
                    TabCode.REGISTER_CONFIRM.FRAGMENT = ConfirmParcelFragment.newInstance(register = registerParcel, beforeStep = NavigatorConst.REGISTER_INPUT_INFO)
                    FragmentManager.move(motherView, TabCode.REGISTER_CONFIRM, RegisterMainFragment.viewId)
                }
            }
        }
    }

    override fun onShowKeyboard()
    {
        motherView.hideTab()
    }
    override fun onHideKeyboard()
    {
        motherView.showTab()
    }

    override fun onResume()
    {
        super.onResume()

        CoroutineScope(Dispatchers.Default).launch {
            ClipboardUtil.pasteClipboardText(context = requireContext())?.let {
                SopoLog.d("클립보드 데이터 $it")
                vm.clipboardText.postValue(it)
            }
        }

    }

    companion object
    {
        fun newInstance(returnType: ReturnType, register: Parcel.Register? = null, parcel: Parcel.Common? = null): InputParcelFragment
        {
            val args = Bundle().apply {
                putSerializable(RegisterMainFragment.REGISTER_INFO, register)
                putSerializable(RegisterMainFragment.PARCEL, parcel)
                putSerializable(RegisterMainFragment.RETURN_TYPE, returnType)
            }

            return InputParcelFragment().apply {
                arguments = args
            }
        }
    }


}