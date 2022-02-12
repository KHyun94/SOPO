package com.delivery.sopo.views.registers

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.databinding.FragmentConfirmParcelBinding
import com.delivery.sopo.enums.NavigatorEnum
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.*
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.mapper.CarrierMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.services.receivers.RefreshParcelBroadcastReceiver
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

    private lateinit var parcelRegister: Parcel.Register
    private var beforeStep: Int = 0

    override fun receiveData(bundle: Bundle)
    {
        super.receiveData(bundle)

        try
        {
            val parcelSerializable = bundle.getSerializable(RegisterMainFragment.REGISTER_INFO) ?: throw NullPointerException("등록 데이터가 정상적으로 오지 않았습니다.")

            if(parcelSerializable !is Parcel.Register) throw IllegalArgumentException("등록 데이터가 정상적으로 오지 않았습니다.")

            parcelRegister = parcelSerializable
            beforeStep = bundle.getInt("beforeStep")

            parcelRegister.waybillNum.let { waybillNo ->
                requireNotNull(waybillNo)
                vm.waybillNum.value = waybillNo
            }

            parcelRegister.carrier.let { carrierEnum ->
                requireNotNull(carrierEnum)
                vm.carrier.value = CarrierMapper.enumToObject(carrierEnum)
            }

            parcelRegister.alias?.let { alias -> vm.alias.value = alias }
        }
        catch(e: Exception)
        {
            SopoLog.e("등록 3단계 실패 - 데이터를 정상적으로 받지 못했습니다.", e)
            alertErrorMessage()
        }

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
                    0->
                    {
                        TabCode.REGISTER_INPUT.FRAGMENT = InputParcelFragment.newInstance(parcelRegister = parcelRegister, returnType = 0)
                        FragmentManager.move(requireActivity(), TabCode.REGISTER_INPUT, RegisterMainFragment.viewId)
                    }
                    1->
                    {
                        TabCode.REGISTER_SELECT.FRAGMENT = InputParcelFragment.newInstance(parcelRegister = parcelRegister, returnType = 0)
                        FragmentManager.move(requireActivity(), TabCode.REGISTER_SELECT, RegisterMainFragment.viewId)
                    }
                }
            }
        }
    }

    private fun alertErrorMessage(){
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
        })).show(parentFragmentManager, "DIALOG")
    }


    override fun setObserve()
    {
        super.setObserve()

        activity ?: return
        parentView.currentPage.observe(this) {
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
                    InputParcelFragment.newInstance(parcelRegister, RegisterMainFragment.REGISTER_PROCESS_RESET)
                }
                NavigatorEnum.REGISTER_INPUT_SUCCESS ->
                {
                    val intent  = Intent(RefreshParcelBroadcastReceiver.ACTION)
                    intent.putExtra("TYPE", 3)
                    parentView.sendBroadcast(intent)

                    val defer = FirebaseRepository.subscribedToTopic()
                    defer.start()

                    InputParcelFragment.newInstance(null, RegisterMainFragment.REGISTER_PROCESS_SUCCESS)
                }
                else -> throw Exception("NOT SUPPORT REGISTER TYPE")
            }

//            FragmentManager.remove(requireActivity())
            FragmentManager.move(requireActivity(), TabCode.REGISTER_INPUT, RegisterMainFragment.viewId)

           /* FragmentManager.initFragment(activity = requireActivity(),
                                         viewId = RegisterMainFragment.viewId,
                                         currentFragment = this@ConfirmParcelFragment,
                                         nextFragment = TabCode.REGISTER_INPUT.FRAGMENT,
                                         nextFragmentTag = TabCode.REGISTER_INPUT.NAME)*/

        })

    }

    companion object
    {
        fun newInstance(register: Parcel.Register?, beforeStep:Int): ConfirmParcelFragment
        {
            val args = Bundle().apply {
                putSerializable(RegisterMainFragment.REGISTER_INFO, register)
                putInt("beforeStep", beforeStep)
            }

            return ConfirmParcelFragment().apply {
                arguments = args
            }
        }
    }


}