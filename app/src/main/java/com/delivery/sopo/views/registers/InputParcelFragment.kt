package com.delivery.sopo.views.registers

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.data.repository.database.room.RoomActivate
import com.delivery.sopo.data.repository.local.repository.CarrierRepository
import com.delivery.sopo.databinding.FragmentInputParcelBinding
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.isGreaterThanOrEqual
import com.delivery.sopo.models.CarrierDTO
import com.delivery.sopo.util.ClipboardUtil
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomAlertMsg
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.viewmodels.registesrs.InputParcelViewModel
import com.delivery.sopo.views.main.MainView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.system.exitProcess

/**
 * 택배 등록 1단
 */
class InputParcelFragment: Fragment()
{
    private lateinit var parentView: MainView
    private lateinit var binding: FragmentInputParcelBinding

    private val vm: InputParcelViewModel by viewModel()

    private val carrierRepo: CarrierRepository by inject()

    private var waybillNum: String? = null
    private var carrierDTO: CarrierDTO? = null
    private var returnType: Int? = null

    lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context)
    {
        super.onAttach(context)

        var pressedTime: Long = 0

        callback = object: OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                if (System.currentTimeMillis() - pressedTime > 2000)
                {
                    pressedTime = System.currentTimeMillis()
                    Snackbar.make(
                        parentView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다.", 2000
                    ).let { bar ->
                        bar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
                    }
                }
                else
                {
                    ActivityCompat.finishAffinity(activity!!)
                    exitProcess(0)
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        parentView = activity as MainView

        // 다른 화면에서 1단계로 다시 이동할 때 전달받은 값
        arguments?.run {
            waybillNum = getString("waybillNum")
            carrierDTO = getSerializable("carrier") as CarrierDTO?
            returnType = getInt("returnType") ?: 0
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentInputParcelBinding.inflate(inflater, container, false)
        binding.vm = vm
        binding.lifecycleOwner = this

        waybillNum?.let {
            if(it.isNotEmpty())
            {
                binding.layoutWaybillNum.hint = ""
            }
        }

        vm.waybillNum.postValue(waybillNum ?: "")
        vm.carrierDTO.postValue(carrierDTO)

        setObserve()
        moveToInquiryTab()

        binding.layoutMainRegister.setOnClickListener {
            Toast.makeText(requireContext(), "백그라운드 클릭", Toast.LENGTH_LONG).show()
            it.requestFocus()
            OtherUtil.hideKeyboardSoft(requireActivity())
        }

        return binding.root
    }

    override fun onDetach()
    {
        super.onDetach()
        callback.remove()
    }

    // 등록 완료 시 조회탭으로 이동
    private fun moveToInquiryTab()
    {
        if (returnType != null && returnType == 1) Handler().post { parentView.onCompleteRegister() }
    }

    private fun setObserve()
    {
        parentView.currentPage.observe(this, Observer {
            if (it != null && it == 0)
            {
                requireActivity().onBackPressedDispatcher.addCallback(this, callback)
            }
        })

        vm.waybillNum.observe(this, Observer { waybillNum ->

            if (waybillNum == null) return@Observer

            if (waybillNum.isNotEmpty()) vm.clipboardText.value = ""

            if (carrierDTO != null) return@Observer

            if (!waybillNum.isGreaterThanOrEqual(9))
            {
                SopoLog.d("input waybill num's length < 9. ")
                binding.vm!!.carrierDTO.value = null
                return@Observer
            }

            CoroutineScope(Dispatchers.Default).launch {
                val carrierList = RoomActivate.recommendAutoCarrier(waybillNum, 1, carrierRepo)

                SopoLog.d("input waybill num's length >= 9. Select Carrier:[${carrierList?.joinToString()}]")

                if (carrierList != null && carrierList.size > 0)
                {
                    SopoLog.d("운송사 리스트 >>> ${carrierList.joinToString()}")
                    binding.vm!!.carrierDTO.postValue(carrierList[0])
                }
            }
        })

        binding.vm!!.focus.observe(this, Observer { focus ->
            val res = TextInputUtil.changeFocus(requireContext(), focus)
            CoroutineScope(Dispatchers.Main).launch {
                binding.vm!!.validates[res.first] = res.second
            }
        })

        binding.vm!!.validateError.observe(this, Observer { target ->
            val message = when (target.first)
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
        })

        binding.vm!!.errorMsg.observe(this, Observer {
            if (!it.isNullOrEmpty())
            {
                CustomAlertMsg.floatingUpperSnackBAr(requireContext(), it, true)
                binding.vm!!.errorMsg.value = ""
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

        binding.vm!!.moveFragment.observe(this, Observer {
            when (it)
            {
                TabCode.REGISTER_STEP2.NAME ->
                {
                    SopoLog.d(
                        """
                        운송장 번호 >>> ${binding.vm!!.waybillNum.value ?: "미입력"}
                        택배사 >>> ${binding.vm!!.carrierDTO.value ?: "미선택"}
                    """.trimIndent()
                    )
                    TabCode.REGISTER_STEP2.FRAGMENT =
                        SelectCarrierFragment.newInstance(binding.vm!!.waybillNum.value, binding.vm!!.carrierDTO.value)
                    FragmentManager.move(parentView, TabCode.REGISTER_STEP2, RegisterMainFragment.layoutId)
                    binding.vm!!.moveFragment.value = ""
                }
                TabCode.REGISTER_STEP3.NAME ->
                {
                    TabCode.REGISTER_STEP3.FRAGMENT =
                        ConfirmParcelFragment.newInstance(binding.vm!!.waybillNum.value, binding.vm!!.carrierDTO.value)
                    FragmentManager.move(parentView, TabCode.REGISTER_STEP3, RegisterMainFragment.layoutId)
                    binding.vm!!.moveFragment.value = ""
                }
            }
        })
    }

    override fun onResume()
    {
        super.onResume()

        SopoLog.d(msg = "OnResume")

        // 0922 kh 추가사항 - 클립보드에 저장되어있는 운송장 번호가 로컬에 등록된 택배가 있을 때, 안띄어주는 로직 추가

        CoroutineScope(Dispatchers.Main).launch {
            val clipboardText = ClipboardUtil.pasteClipboardText(SOPOApp.INSTANCE)?:return@launch

            val isRegister = binding.vm!!.waybillNum.value.isNullOrEmpty()

            if ((clipboardText.isEmpty() || !isRegister).not())
            {
                binding.vm!!.clipboardText.postValue(clipboardText)
            }
        }
    }

    companion object
    {
        fun newInstance(waybillNum: String?, carrierDTO: CarrierDTO?, returnType: Int?): InputParcelFragment
        {
            val registerStep1 = InputParcelFragment()

            val args = Bundle()

            args.putString("waybillNum", waybillNum)
            args.putSerializable("carrier", carrierDTO)
            // 다른 프래그먼트에서 돌아왔을 때 분기 처리
            // 0: Default 1: Success To Register
            args.putInt("returnType", returnType ?: 0)

            registerStep1.arguments = args
            return registerStep1
        }
    }
}