package com.delivery.sopo.presentation.register.view

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentInputParcelBinding
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.SnackBarType
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.isGreaterThanOrEqual
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.presentation.consts.IntentConst
import com.delivery.sopo.presentation.models.enums.RegisterNavigation
import com.delivery.sopo.presentation.register.viewmodel.InputParcelViewModel
import com.delivery.sopo.presentation.views.main.MainActivity
import com.delivery.sopo.util.*
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InputParcelFragment : BaseFragment<FragmentInputParcelBinding, InputParcelViewModel>() {
    override val layoutRes: Int = R.layout.fragment_input_parcel
    override val vm: InputParcelViewModel by viewModels()
    override val mainLayout: View by lazy { binding.constraintMainRegister }

    private val motherActivity: MainActivity by lazy { activity as MainActivity }

    private lateinit var registerNavigation: RegisterNavigation

    override fun receiveData(bundle: Bundle) {
        super.receiveData(bundle)

        registerNavigation = bundle.getSerializable(RegisterParcelFragment.RETURN_TYPE) as RegisterNavigation
    }

    override fun setBeforeBinding() {
        super.setBeforeBinding()

        onSOPOBackPressedListener = object : OnSOPOBackPressEvent() {
            override fun onBackPressedInTime() {
                Snackbar.make(motherActivity.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다.", 2000)
                    .apply { animationMode = Snackbar.ANIMATION_MODE_SLIDE }
                    .show()
            }

            override fun onBackPressedOutTime() {
                exit()
            }
        }
    }

    override fun setAfterBinding() {
        super.setAfterBinding()

        binding.constraintMainRegister.setOnClickListener {
            it.requestFocus()
            OtherUtil.hideKeyboardSoft(requireActivity())
        }

        when (registerNavigation) {
            is RegisterNavigation.Init -> {
            }
            is RegisterNavigation.Next -> {
                vm.parcel = (registerNavigation as RegisterNavigation.Next).parcel.apply {
                    vm.waybillNum.postValue(waybillNum)
                    vm.carrier.postValue(carrier)
                }
            }
            is RegisterNavigation.Complete -> {
                notifyRegisteredParcel((registerNavigation as RegisterNavigation.Complete).parcel)
            }
        }
    }

    override fun setObserve() {
        super.setObserve()

        activity ?: return

        motherActivity.getCurrentPage().observe(this) {
            if (it != TabCode.REGISTER_TAB) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.focus.observe(this) { focus ->
            val res = TextInputUtil.changeFocus(requireContext(), focus)
            vm.validity[res.first] = res.second
        }

        vm.waybillNum.observe(this) { waybillNum ->
            if (waybillNum.isEmpty()) return@observe
            vm.clipboardText.value = ""

            if (!binding.layoutWaybillNum.hasFocus()) binding.layoutWaybillNum.requestFocus()

            if (!waybillNum.isGreaterThanOrEqual(8)) {
                vm.carrier.value = null
                return@observe
            }

//            vm.recommendCarrier(waybillNum)
        }

        vm.invalidity.observe(this) { target ->

            val message = when (target.first) {
                InfoEnum.WAYBILL_NUMBER -> {
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

            val fm = parentFragmentManager.beginTransaction()

            when(nav){
                is RegisterNavigation.Init, is RegisterNavigation.Complete -> throw IllegalAccessException()
                is RegisterNavigation.Next ->
                {
                    if(nav.nextStep == "SELECT_CARRIER") SelectCarrierFragment.newInstance(nav)
                    fm.replace(RegisterParcelFragment.viewId, SelectCarrierFragment.newInstance(nav))
                    fm.commit()
                }
            }
        }
    }

    override fun onShowKeyboard() {
        motherActivity.hideTab()
    }

    override fun onHideKeyboard() {
        motherActivity.showTab()
    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.Default).launch {
            ClipboardUtil.pasteClipboardText(context = requireContext())?.let {
                SopoLog.d("클립보드 데이터 $it")
                vm.clipboardText.postValue(it)
            }
        }
    }

    fun notifyRegisteredParcel(parcel:Parcel.Common)
    {
        val intent = Intent()

        if(parcel.isDelivered())
        {
            val date = parcel.getDeliveredAlarm()

            motherActivity.onMake(SnackBarType.Update(content = "${date}에 배송완료된 택배네요.", duration = 3000, buttonContent = "보기", iconRes = R.drawable.ic_right_arrow_blue_scale){
                motherActivity.onSetCurrentPage(1)

                intent.action = IntentConst.Action.REGISTERED_COMPLETED_PARCEL
                intent.putExtra(IntentConst.Extra.REGISTERED_DATE, date)
                motherActivity.sendBroadcast(intent)
            })
        }
        else
        {
            motherActivity.onMake(SnackBarType.Update(content = "택배가 등록되었습니다.", duration = 3000, buttonContent = "보기", iconRes = R.drawable.ic_right_arrow_blue_scale){

                motherActivity.onSetCurrentPage(1)

                intent.action = IntentConst.Action.REGISTERED_ONGOING_PARCEL
                motherActivity.sendBroadcast(intent)
            })
        }

        motherActivity.onShow()
    }

    companion object {
        fun newInstance(
            registerNavigation: RegisterNavigation
        ): InputParcelFragment {
            val args = Bundle().apply {
                putSerializable(RegisterParcelFragment.RETURN_TYPE, registerNavigation)
            }

            return InputParcelFragment().apply {
                arguments = args
            }
        }
    }

}