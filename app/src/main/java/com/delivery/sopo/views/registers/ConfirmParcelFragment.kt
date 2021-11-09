package com.delivery.sopo.views.registers

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.databinding.FragmentConfirmParcelBinding
import com.delivery.sopo.enums.NavigatorEnum
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressListener
import com.delivery.sopo.models.*
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.mapper.CarrierMapper
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.registesrs.ConfirmParcelViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.dialog.OnAgreeClickListener
import com.delivery.sopo.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConfirmParcelFragment: BaseFragment<FragmentConfirmParcelBinding, ConfirmParcelViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_confirm_parcel
    override val vm: ConfirmParcelViewModel by viewModel()
    override val mainLayout: View by lazy { binding.constraintMainConfirmParcel }

    private val parentView: MainView by lazy { activity as MainView }

    private lateinit var register: ParcelRegister

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        onSOPOBackPressedListener = object: OnSOPOBackPressListener
        {
            override fun onBackPressedInTime()
            {
                requireActivity().supportFragmentManager.popBackStack()
            }

            override fun onBackPressedOutTime()
            {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    override fun receiveData(bundle: Bundle)
    {
        super.receiveData(bundle)

        try
        {
            register = bundle.getSerializable(RegisterMainFragment.REGISTER_INFO) as ParcelRegister

            register.waybillNum.let { waybillNo ->
                requireNotNull(waybillNo)
                vm.waybillNum.value = waybillNo
            }

            register.carrier.let { carrierEnum ->
                requireNotNull(carrierEnum)
                vm.carrier.value = CarrierMapper.enumToObject(carrierEnum)
            }

            register.alias?.let { alias -> vm.alias.value = alias }
        }
        catch(e: Exception)
        {
            SopoLog.e("등록 3단계 실패 - 데이터를 정상적으로 받지 못했습니다.", e)

            alertErrorMessage()
        }

    }


    fun alertErrorMessage(){
        GeneralDialog(requireActivity(), "등록 오류", "시스템 오류로 다시 입력을 부탁드립니다. ㅠㅡㅜ", null, Pair("이동", object: OnAgreeClickListener
        {
            override fun invoke(agree: GeneralDialog)
            {
                TabCode.REGISTER_INPUT.FRAGMENT = InputParcelFragment.newInstance(null, RegisterMainFragment.REGISTER_PROCESS_RESET)

                FragmentManager.initFragment(activity = requireActivity(),
                                             viewId = RegisterMainFragment.viewId,
                                             currentFragment = this@ConfirmParcelFragment,
                                             nextFragment = TabCode.REGISTER_INPUT.FRAGMENT,
                                             nextFragmentTag = TabCode.REGISTER_INPUT.NAME)
            }
        })).show(requireFragmentManager(), "DIALOG")
    }

    override fun setObserve()
    {
        super.setObserve()
        parentView.currentPage.observe(this) {

            it ?: return@observe
            if(it != 0) return@observe

            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.navigator.observe(this, Observer { navigator ->

            TabCode.REGISTER_INPUT.FRAGMENT  = when(navigator)
            {
                NavigatorEnum.REGISTER_INPUT_INIT ->
                {
                    InputParcelFragment.newInstance(null, RegisterMainFragment.REGISTER_PROCESS_RESET)
                }
                NavigatorEnum.REGISTER_INPUT_REVISE ->
                {
                    InputParcelFragment.newInstance(register, RegisterMainFragment.REGISTER_PROCESS_RESET)
                }
                NavigatorEnum.REGISTER_INPUT_SUCCESS ->
                {
                    val defer = FirebaseRepository.subscribedToTopic()
                    defer.start()

                    InputParcelFragment.newInstance(null, RegisterMainFragment.REGISTER_PROCESS_SUCCESS)
                }
                else -> throw Exception("NOT SUPPORT REGISTER TYPE")
            }

            FragmentManager.initFragment(activity = requireActivity(),
                                         viewId = RegisterMainFragment.viewId,
                                         currentFragment = this@ConfirmParcelFragment,
                                         nextFragment = TabCode.REGISTER_INPUT.FRAGMENT,
                                         nextFragmentTag = TabCode.REGISTER_INPUT.NAME)

        })

    }

    companion object
    {
        fun newInstance(register: ParcelRegister?): ConfirmParcelFragment
        {
            val args = Bundle().apply {
                putSerializable(RegisterMainFragment.REGISTER_INFO, register)
            }

            return ConfirmParcelFragment().apply {
                arguments = args
            }
        }
    }


}