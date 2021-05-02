package com.delivery.sopo.views.registers

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.database.room.RoomActivate
import com.delivery.sopo.databinding.RegisterStep1Binding
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.isGreaterThanOrEqual
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.repository.impl.CourierRepoImpl
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.util.ClipboardUtil
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomAlertMsg
import com.delivery.sopo.util.ui_util.TextInputUtil
import com.delivery.sopo.viewmodels.registesrs.RegisterStep1ViewModel
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.widget.CustomEditText.Companion.STATUS_COLOR_BLUE
import com.delivery.sopo.views.widget.CustomEditText.Companion.STATUS_COLOR_ELSE
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

typealias FocusChangeCallback = (String, Boolean) -> Unit

class RegisterStep1: Fragment()
{
    private lateinit var parentView: MainView

    private lateinit var binding: RegisterStep1Binding
    private val vm: RegisterStep1ViewModel by viewModel()

    private val courierRepoImpl: CourierRepoImpl by inject()
    private val parcelRepoImpl: ParcelRepoImpl by inject()

    private var wayBilNum: String? = null
    private var courier: CourierItem? = null
    private var returnType: Int? = null

    // todo 추 후 각 페이지에 중복되어있는 로직을 통합 처리 예정
    var callback: OnBackPressedCallback? = null

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
                    val snackbar = Snackbar.make(
                        parentView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다.", 2000
                    )
                    snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()

                    SopoLog.d("RegisterStep1::1 BackPressListener = 종료를 위해 한번 더 클릭")
                }
                else
                {
                    SopoLog.d("RegisterStep1::1 BackPressListener = 종료")
                    ActivityCompat.finishAffinity(activity!!)
                    System.exit(0)
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback!!)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        parentView = activity as MainView

        // 다른 화면에서 1단계로 다시 이동할 때 전달받은 값
        arguments?.run {
            wayBilNum = getString("wayBilNum") ?: ""
            courier = getSerializable("courier") as CourierItem?
            returnType = getInt("returnType") ?: 0

            SopoLog.d(
                """
                RegisterStep1
                운송장번호 >>> ${wayBilNum}
                택배사 >>> ${courier}
                반환 타입 >>> ${returnType}
            """.trimIndent()
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = RegisterStep1Binding.inflate(inflater, container, false)
        binding.vm = vm
        binding.lifecycleOwner = this

        binding.vm!!.wayBilNum.postValue(wayBilNum ?: "")
        binding.vm!!.courier.postValue(courier)

        setObserve()
        moveToInquiryTab()

        return binding.root
    }

    override fun onDetach()
    {
        super.onDetach()
        callback?.remove()
    }

    // 등록 완료 시 조회탭으로 이동
    private fun moveToInquiryTab()
    {
        if (returnType != null && returnType == 1) Handler().post { parentView.onCompleteRegister() }
    }

    private fun setObserve()
    {
        var pressedTime: Long = 0

        parentView.currentPage.observe(this, Observer {
            if (it != null && it == 0)
            {
                callback = object: OnBackPressedCallback(true)
                {
                    override fun handleOnBackPressed()
                    {
                        SopoLog.d(msg = "RegisterStep1:: BackPressListener")

                        if (System.currentTimeMillis() - pressedTime > 2000)
                        {
                            pressedTime = System.currentTimeMillis()
                            val snackbar = Snackbar.make(
                                parentView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다.", 2000
                            )
                            snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()

                            SopoLog.d("MenuFragment::1 BackPressListener = 종료를 위해 한번 더 클릭")
                        }
                        else
                        {
                            SopoLog.d("RegisterStep1::1 BackPressListener = 종료")
                            ActivityCompat.finishAffinity(activity!!)
                            System.exit(0)
                        }

                    }

                }

                requireActivity().onBackPressedDispatcher.addCallback(this, callback!!)
            }
        })


        binding.vm!!.wayBilNum.observe(this, Observer { wayBilNum ->

            if (wayBilNum == null) return@Observer

            if (wayBilNum.isNotEmpty()) binding.vm!!.clipBoardWords.value = ""

            if (courier == null)
            {
                if (!wayBilNum.isGreaterThanOrEqual(9))
                {
                    binding.vm!!.courier.value = null
                    return@Observer
                }

                val courierList =
                    RoomActivate.recommendAutoCourier(SOPOApp.INSTANCE, wayBilNum, 1, courierRepoImpl)

                if (courierList != null && courierList.size > 0)
                {
                    SopoLog.d(
                        msg = """
                        최우선 순위 >>> ${courierList[0]}
                    """.trimIndent()
                    )

                    binding.vm!!.courier.value = (courierList[0])
                }
            }
        })

        binding.vm!!.focus.observe(this, Observer { focus ->
            val res = TextInputUtil.changeFocus(requireContext(), focus)
            binding.vm!!.validates[res.first] = res.second
        })

        binding.vm!!.validateError.observe(this, Observer { target ->
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
                        운송장 번호 >>> ${binding.vm!!.wayBilNum.value ?: "미입력"}
                        택배사 >>> ${binding.vm!!.courier.value ?: "미선택"}
                    """.trimIndent()
                    )
                    TabCode.REGISTER_STEP2.FRAGMENT =
                        RegisterStep2.newInstance(binding.vm!!.wayBilNum.value, binding.vm!!.courier.value)
                    FragmentManager.move(parentView, TabCode.REGISTER_STEP2, RegisterMainFrame.viewId)
                    binding.vm!!.moveFragment.value = ""
                }
                TabCode.REGISTER_STEP3.NAME ->
                {
                    TabCode.REGISTER_STEP3.FRAGMENT =
                        RegisterStep3.newInstance(binding.vm!!.wayBilNum.value, binding.vm!!.courier.value)
                    FragmentManager.move(parentView, TabCode.REGISTER_STEP3, RegisterMainFrame.viewId)
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
        ClipboardUtil.pasteClipboardText(SOPOApp.INSTANCE, parcelRepoImpl) {
            val isRegister = binding.vm!!.wayBilNum.value.isNullOrEmpty()

            if (!(it.isEmpty() || !isRegister))
            {
                SopoLog.d(msg = "복사된 클립보드 >>> $it")
                binding.vm!!.clipBoardWords.postValue(it)
            }
        }
    }

    companion object
    {
        fun newInstance(wayBilNum: String?, courier: CourierItem?, returnType: Int?): RegisterStep1
        {
            val registerStep1 = RegisterStep1()

            val args = Bundle()

            args.putString("wayBilNum", wayBilNum)
            args.putSerializable("courier", courier)
            // 다른 프래그먼트에서 돌아왔을 때 분기 처리
            // 0: Default 1: Success To Register
            args.putInt("returnType", returnType ?: 0)

            registerStep1.arguments = args
            return registerStep1
        }
    }
}