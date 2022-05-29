package com.delivery.sopo.presentation.views.registers

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.FragmentConfirmParcelBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.mapper.CarrierMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.presentation.models.enums.ReturnType
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.KeyboardVisibilityUtil
import com.delivery.sopo.presentation.viewmodels.registesrs.ConfirmParcelViewModel
import com.delivery.sopo.presentation.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConfirmParcelFragment: BaseFragment<FragmentConfirmParcelBinding, ConfirmParcelViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_confirm_parcel
    override val vm: ConfirmParcelViewModel by viewModel()
    override val mainLayout: View by lazy { binding.constraintMainConfirmParcel }

    private val motherView: MainView by lazy { activity as MainView }

    private lateinit var registerInfo: Parcel.Register
    private lateinit var beforeStep: String

    private lateinit var keyboardVisibilityUtil: KeyboardVisibilityUtil

    override fun receiveData(bundle: Bundle)
    {
        super.receiveData(bundle)

        val registerInfo = bundle.getSerializable(RegisterMainFragment.REGISTER_INFO)

        if(registerInfo !is Parcel.Register) throw IllegalArgumentException("등록 데이터가 정상적으로 오지 않았습니다.")

        this.registerInfo = registerInfo
        beforeStep = bundle.getString(RegisterMainFragment.BEFORE_STEP) ?: return

        this.registerInfo.waybillNum.let { waybillNum -> vm.waybillNum.value = waybillNum }
        this.registerInfo.carrier?.let { carrier -> vm.carrier.value = CarrierMapper.enumToObject(carrier) }
        this.registerInfo.alias?.let { alias -> vm.alias.value = alias }
    }

    override fun setBeforeBinding()
    {
        super.setBeforeBinding()

        useCommonBackPressListener(isUseCommon = true)

        onSOPOBackPressedListener = object: OnSOPOBackPressEvent(isUseCommon = true)
        {
            override fun onBackPressed()
            {
                super.onBackPressed()

                when(beforeStep)
                {
                    NavigatorConst.REGISTER_INPUT_INFO ->
                    {
                        TabCode.REGISTER_INPUT.FRAGMENT = InputParcelFragment.newInstance(register = registerInfo, returnType = ReturnType.REVISE_PARCEL)
                        FragmentManager.move(requireActivity(), TabCode.REGISTER_INPUT, RegisterMainFragment.viewId)
                    }
                    NavigatorConst.REGISTER_SELECT_CARRIER ->
                    {
                        TabCode.REGISTER_SELECT.FRAGMENT = SelectCarrierFragment.newInstance(registerInfo.waybillNum?:"")
                        FragmentManager.move(requireActivity(), TabCode.REGISTER_SELECT, RegisterMainFragment.viewId)
                    }
                }
            }
        }

        keyboardVisibilityUtil = KeyboardVisibilityUtil(requireActivity().window,
                                                          onShowKeyboard = {
                                                              motherView.hideTab()
                                                          },
                                                          onHideKeyboard = {
                                                              motherView.showTab()
                                                          }
        )
    }

    override fun setObserve()
    {
        super.setObserve()

        activity ?: return

        motherView.getCurrentPage().observe(this) {
            if(it != TabCode.firstTab) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.navigator.observe(this) { navigator ->

            TabCode.REGISTER_INPUT.FRAGMENT = when(navigator)
            {
                NavigatorConst.REGISTER_INITIALIZE ->
                {
                    InputParcelFragment.newInstance(returnType = ReturnType.INIT_PARCEL)
                }
                NavigatorConst.REGISTER_REVISE ->
                {
                    InputParcelFragment.newInstance(register = registerInfo, returnType = ReturnType.REVISE_PARCEL)
                }
                NavigatorConst.REGISTER_SUCCESS ->
                {
                    InputParcelFragment.newInstance(parcel = vm.parcel, returnType = ReturnType.COMPLETE_REGISTER_PARCEL)
                }
                else -> throw Exception("NOT SUPPORT REGISTER TYPE")
            }

            FragmentManager.move(requireActivity(), TabCode.REGISTER_INPUT, RegisterMainFragment.viewId)
        }

        vm.alias.observe(this) { alias ->
            if(alias.length >= 15)
            {
                showToast("15자 이내로 입력이 가능합니다.", Toast.LENGTH_SHORT)
            }
        }
    }

    override fun onDestroy()
    {
        keyboardVisibilityUtil.detachKeyboardListeners()
        super.onDestroy()
    }

    companion object
    {
        fun newInstance(register: Parcel.Register?, beforeStep: String): ConfirmParcelFragment
        {
            val args = Bundle().apply {
                putSerializable(RegisterMainFragment.REGISTER_INFO, register)
                putString(RegisterMainFragment.BEFORE_STEP, beforeStep)
            }

            return ConfirmParcelFragment().apply {
                arguments = args
            }
        }
    }


}