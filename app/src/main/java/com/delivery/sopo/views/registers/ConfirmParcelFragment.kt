package com.delivery.sopo.views.registers

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.FragmentConfirmParcelBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.mapper.CarrierMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.services.receivers.RefreshParcelBroadcastReceiver
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.KeyboardVisibilityUtil
import com.delivery.sopo.viewmodels.registesrs.ConfirmParcelViewModel
import com.delivery.sopo.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConfirmParcelFragment: BaseFragment<FragmentConfirmParcelBinding, ConfirmParcelViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_confirm_parcel
    override val vm: ConfirmParcelViewModel by viewModel()
    override val mainLayout: View by lazy { binding.constraintMainConfirmParcel }

    private val parentView: MainView by lazy { activity as MainView }

    private lateinit var parcelRegister: Parcel.Register
    private var beforeStep: String = ""

    private lateinit var keyboardVisibilityUtil: KeyboardVisibilityUtil

    override fun receiveData(bundle: Bundle)
    {
        super.receiveData(bundle)

        val parcelSerializable = bundle.getSerializable(RegisterMainFragment.REGISTER_INFO)

        if(parcelSerializable !is Parcel.Register) throw IllegalArgumentException("등록 데이터가 정상적으로 오지 않았습니다.")

        parcelRegister = parcelSerializable
        beforeStep = bundle.getString("BEFORE_STEP") ?: return

        parcelRegister.waybillNum.let { waybillNum ->
            vm.waybillNum.value = waybillNum
        }

        parcelRegister.carrier?.let { carrier ->
            vm.carrier.value = CarrierMapper.enumToObject(carrier)
        }

        parcelRegister.alias?.let { alias -> vm.alias.value = alias }
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
                        TabCode.REGISTER_INPUT.FRAGMENT =
                            InputParcelFragment.newInstance(parcelRegister = parcelRegister, returnType = 0)
                        FragmentManager.move(requireActivity(), TabCode.REGISTER_INPUT, RegisterMainFragment.viewId)
                    }
                    NavigatorConst.REGISTER_SELECT_CARRIER ->
                    {
                        TabCode.REGISTER_SELECT.FRAGMENT =
                            InputParcelFragment.newInstance(parcelRegister = parcelRegister, returnType = 0)
                        FragmentManager.move(requireActivity(), TabCode.REGISTER_SELECT, RegisterMainFragment.viewId)
                    }
                }
            }
        }

        keyboardVisibilityUtil = KeyboardVisibilityUtil(requireActivity().window,
                                                          onShowKeyboard = {
                                                              parentView.hideTab()
                                                          },
                                                          onHideKeyboard = {
                                                              parentView.showTab()
                                                          }
        )

    }

    override fun setObserve()
    {
        super.setObserve()

        activity ?: return

        parentView.getCurrentPage().observe(this) {
            if(it != TabCode.firstTab) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.navigator.observe(this) { navigator ->

            TabCode.REGISTER_INPUT.FRAGMENT = when(navigator)
            {
                NavigatorConst.REGISTER_INITIALIZE ->
                {
                    InputParcelFragment.newInstance(null, RegisterMainFragment.REGISTER_PROCESS_RESET)
                }
                NavigatorConst.REGISTER_REVISE ->
                {
                    InputParcelFragment.newInstance(parcelRegister, RegisterMainFragment.REGISTER_PROCESS_RESET)
                }
                NavigatorConst.REGISTER_SUCCESS ->
                {
                    val intent = Intent(RefreshParcelBroadcastReceiver.COMPLETE_REGISTER_ACTION)
                    intent.putExtra("PARCEL_ID", vm.parcelId)
                    requireActivity().sendBroadcast(intent)

                    InputParcelFragment.newInstance(null, RegisterMainFragment.REGISTER_PROCESS_SUCCESS)
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
                putString("BEFORE_STEP", beforeStep)
            }

            return ConfirmParcelFragment().apply {
                arguments = args
            }
        }
    }


}