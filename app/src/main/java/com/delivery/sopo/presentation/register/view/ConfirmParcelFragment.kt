package com.delivery.sopo.presentation.register.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.delivery.sopo.R
import com.delivery.sopo.data.models.Result
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.databinding.FragmentConfirmParcelBinding
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.mapper.CarrierMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.presentation.register.viewmodel.ConfirmParcelViewModel
import com.delivery.sopo.presentation.views.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConfirmParcelFragment: BaseFragment<FragmentConfirmParcelBinding, ConfirmParcelViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_confirm_parcel
    override val vm: ConfirmParcelViewModel by viewModels()
    override val mainLayout: View by lazy { binding.constraintMainConfirmParcel }

    private val motherActivity: MainActivity by lazy { activity as MainActivity }

    private lateinit var registerInfo: Parcel.Register
    private lateinit var beforeStep: String

    override fun receiveData(bundle: Bundle)
    {
        super.receiveData(bundle)

        val registerInfo = bundle.getSerializable(RegisterParcelFragment.REGISTER_INFO)

        if(registerInfo !is Parcel.Register) throw IllegalArgumentException("등록 데이터가 정상적으로 오지 않았습니다.")

        this.registerInfo = registerInfo
        beforeStep = bundle.getString(RegisterParcelFragment.BEFORE_STEP) ?: return

        this.registerInfo.waybillNum.let { waybillNum -> vm.waybillNum.value = waybillNum }
        this.registerInfo.carrier?.let { carrier ->
            vm.carrier.value = CarrierMapper.enumToObject(carrier)
        }
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
//                        TabCode.REGISTER_INPUT.FRAGMENT = InputParcelFragment.newInstance(register = registerInfo, registerNavigation = RegisterNavigation.Next)
//                        FragmentManager.move(requireActivity(), TabCode.REGISTER_INPUT, RegisterParcelFragment.viewId)
                    }
                    NavigatorConst.REGISTER_SELECT_CARRIER ->
                    {
//                        TabCode.REGISTER_SELECT.FRAGMENT = SelectCarrierFragment.newInstance(registerInfo.waybillNum ?: "")
//                        FragmentManager.move(requireActivity(), TabCode.REGISTER_SELECT, RegisterParcelFragment.viewId)
                    }
                }
            }
        }
    }

    override fun setAfterBinding()
    {
        super.setAfterBinding()

        lifecycleScope.launch(Dispatchers.Main) {
            when(registerInfo.carrier)
            {
                CarrierEnum.CHUNILPS ->
                {
                    binding.ivCarrier.setBackgroundResource(R.drawable.ic_thumbnail_chuil_2)
                }
                CarrierEnum.DAESIN ->
                {
                    binding.ivCarrier.setBackgroundResource(R.drawable.ic_thumbnail_daeshin_2)
                }
                CarrierEnum.LOTTE ->
                {
                    binding.ivCarrier.setBackgroundResource(R.drawable.ic_thumbnail_lotte_2)
                }
            }
        }


    }

    override fun setObserve()
    {
        super.setObserve()

        activity ?: return

        motherActivity.getCurrentPage().observe(this) {
            if(it != TabCode.REGISTER_TAB) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.navigator.observe(this) { navigator ->

//            TabCode.REGISTER_INPUT.FRAGMENT = when(navigator)
//            {
//                NavigatorConst.REGISTER_INITIALIZE ->
//                {
//                    InputParcelFragment.newInstance(registerNavigation = RegisterNavigation.Init)
//                }
//                NavigatorConst.REGISTER_REVISE ->
//                {
////                    InputParcelFragment.newInstance(register = registerInfo, registerNavigation = RegisterNavigation.Next)
//                }
//                NavigatorConst.REGISTER_SUCCESS ->
//                {
//                    InputParcelFragment.newInstance(parcel = vm.parcel, registerNavigation = RegisterNavigation.Complete)
//                }
//                else -> throw Exception("NOT SUPPORT REGISTER TYPE")
//            }

            FragmentManager.move(requireActivity(), TabCode.REGISTER_INPUT, RegisterParcelFragment.viewId)
        }

        vm.status.asLiveData(Dispatchers.Main).observe(this) { result ->

            when(result)
            {
                is Result.Success ->
                {
//                    TabCode.REGISTER_INPUT.FRAGMENT = InputParcelFragment.newInstance(parcel = vm.parcel, registerNavigation = RegisterNavigation.Complete)
//                    FragmentManager.move(requireActivity(), TabCode.REGISTER_INPUT, RegisterParcelFragment.viewId)
                }
            }

        }

        vm.alias.observe(this) { alias ->
            if(alias.length >= 15)
            {
                showToast("15자 이내로 입력이 가능합니다.", Toast.LENGTH_SHORT)
            }
        }
    }

    override fun onShowKeyboard()
    {
        motherActivity.hideTab()
    }
    override fun onHideKeyboard()
    {
        motherActivity.showTab()
    }

    companion object
    {
        fun newInstance(register: Parcel.Register?, beforeStep: String): ConfirmParcelFragment
        {
            val args = Bundle().apply {
                putSerializable(RegisterParcelFragment.REGISTER_INFO, register)
                putString(RegisterParcelFragment.BEFORE_STEP, beforeStep)
            }

            return ConfirmParcelFragment().apply {
                arguments = args
            }
        }
    }


}