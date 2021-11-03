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

    override fun receiveData(bundle: Bundle)
    {
        super.receiveData(bundle)

        bundle.getSerializable(RegisterMainFrame.REGISTER_INFO).let { data ->
            if(data !is ParcelRegister) return@let
            parcelRegister = data
        }

        returnType = bundle.getInt(RegisterMainFrame.RETURN_TYPE)
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

        vm.errorMsg.observe(this, Observer {
            if(!it.isNullOrEmpty())
            {
                //                TODO 커스텀 스낵바
                //                CustomSnackBar(context = requireContext()).floatingUpperSnackBAr(requireContext(), it, true)
                vm.errorMsg.value = ""
            }
        })
        /**
         *  1. 송장번호를 입력했을 때 자동으로 택배사를 추천
         *      -> '다음'버튼을 눌렀을 때 Step3로 이동
         *      -> '이 택배사가 아닌가요?' 또는 택배사를 선택했을 때 Step2로 이동
         *      -> Step2에서 택배사를 선택했을 때 Step3로 이동
         *      -> Step3에서 '수정하기'를 선택했을 때 Step1으로 이동
         *      (택배사 정규식에 맞지 않으면 에러 발생)
         *
         *  2. 택배사를 먼저 선택했을 때
         *      -> Step2로 이동
         *      -> 택배사 선택 시 Step1으로 이동
         *      (송장번호에 따라 우선설정되는 택배사를 step2에서 보여주지만 현재
         *      이외 모든 택배사도 보여줌으로 송장번호와 택배사의 정규식이 부합하지 않더라도 등록 가능)
         *
         */

        vm.navigator.observe(this) { nav ->

            val registerDTO = ParcelRegister(vm.waybillNum.value, vm.carrier.value?.carrier, null)

            when(nav)
            {
                NavigatorEnum.REGISTER_SELECT ->
                {
                    SopoLog.d("""
                        운송장 번호 >>> ${vm.waybillNum.value ?: "미입력"}
                        택배사 >>> ${vm.carrier.value ?: "미선택"}
                    """.trimIndent())
                    TabCode.REGISTER_SELECT.FRAGMENT =
                        SelectCarrierFragment.newInstance(vm.waybillNum.value ?: "")
                    FragmentManager.move(parentView, TabCode.REGISTER_SELECT, RegisterMainFrame.viewId)
                }
                NavigatorEnum.REGISTER_CONFIRM ->
                {
                    TabCode.REGISTER_CONFIRM.FRAGMENT =
                        ConfirmParcelFragment.newInstance(register = registerDTO)
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