package com.delivery.sopo.views.registers

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.database.room.RoomActivate
import com.delivery.sopo.databinding.RegisterStep1Binding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.isGreaterThanOrEqual
import com.delivery.sopo.models.BindView
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.repository.impl.CourierRepolmpl
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.util.ClipboardUtil
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomAlertMsg
import com.delivery.sopo.viewmodels.registesrs.RegisterStep1ViewModel
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.widget.CustomEditText.Companion.STATUS_COLOR_BLUE
import com.delivery.sopo.views.widget.CustomEditText.Companion.STATUS_COLOR_ELSE
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.system.exitProcess

class RegisterStep1 : Fragment()
{
    private val TAG = this.javaClass.simpleName

    private lateinit var parentView : MainView

    private lateinit var binding : RegisterStep1Binding
    private val vm : RegisterStep1ViewModel by viewModel()

    private val courierRepoImpl : CourierRepolmpl by inject()
    private val parcelRepoImpl : ParcelRepoImpl by inject()

    private var wayBilNum : String? = null
    private var courier : CourierItem? = null
    private var returnType : Int? = null

    // todo 추 후 각 페이지에 중복되어있는 로직을 통합 처리 예정
    var callback : OnBackPressedCallback? = null

    override fun onAttach(context : Context)
    {
        super.onAttach(context)

        parentView = activity as MainView
        callback = onBackClickListener()

        requireActivity().onBackPressedDispatcher.addCallback(this, callback!!)
    }

    private fun onBackClickListener () : OnBackPressedCallback
    {
        var pressedTime : Long = 0

        return object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                if (System.currentTimeMillis() - pressedTime > 2000)
                {
                    pressedTime = System.currentTimeMillis()
                    Snackbar.make(binding.layoutRegister, "한번 더 누르시면 앱이 종료됩니다.", 2000)
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
                }
                else
                {
                    ActivityCompat.finishAffinity(activity!!)
                    exitProcess(0)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)

        // 다른 화면에서 1단계로 다시 이동할 때 전달받은 값
        if (arguments != null) arguments.run {
            wayBilNum = this?.getString("wayBilNum") ?: ""
            courier = this?.getSerializable("courier") as CourierItem?
            returnType = this?.getInt("returnType") ?: 0
        }
    }

    override fun onCreateView(
        inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?
    ) : View
    {
        binding = RegisterStep1Binding.inflate(inflater, container, false)
        binding.vm = vm

        binding.vm!!.wayBilNum.value = wayBilNum ?: ""
        binding.vm!!.courier.value = courier

        setObserve()
        moveToInquiryTab()

        return binding.root
    }

    override fun onDetach()
    {
        super.onDetach()
        if (callback != null) callback!!.remove()
    }

    // 등록 완료 시 조회탭으로 이동
    private fun moveToInquiryTab()
    {
        if (returnType != null && returnType == 1) Handler().post { parentView.onCompleteRegister() }
    }

    private fun setObserve()
    {
        binding.vm!!.wayBilNum.observe(this, Observer { wayBilNum ->

            if (wayBilNum == null) return@Observer
            if (wayBilNum.isNotEmpty()) binding.vm!!.clipBoardWords.value = ""

            binding.vm!!.wayBilNumStatusType.value =
                if (wayBilNum.isGreaterThanOrEqual(1)) STATUS_COLOR_BLUE
                else STATUS_COLOR_ELSE

            if(!wayBilNum.isGreaterThanOrEqual(9))
                return@Observer

            val courierList = RoomActivate.recommendAutoCourier(SOPOApp.INSTANCE, wayBilNum, 1, courierRepoImpl)

            SopoLog.d(
                tag = TAG, msg = """
                    추천 택배 리스트 
                    {
                    ${courierList?.joinToString(",")}
                    }
                """.trimIndent()
            )

            if (courierList != null && courierList.size > 0)
            {
                SopoLog.d(tag= TAG, msg = """
                        최우선 순위 >>> ${courierList[0]}
                    """.trimIndent())

                binding.vm!!.courier.postValue(courierList[0])
            }
        })

        binding.vm!!.errorMsg.observe(this, Observer {
            if (!it.isNullOrEmpty())
            {
                CustomAlertMsg.floatingUpperSnackBAr(this.context!!, it, true)
                binding.vm!!.errorMsg.value = ""
            }
        })

        binding.vm!!.moveFragment.observe(this, Observer {
            when (it)
            {
                TabCode.REGISTER_STEP2.NAME ->
                {
                    TabCode.REGISTER_STEP2.FRAGMENT = RegisterStep2.newInstance(binding.vm!!.wayBilNum.value, binding.vm!!.courier.value)
                    FragmentManager.move(parentView, TabCode.REGISTER_STEP2, RegisterMainFrame.viewId)
                    binding.vm!!.moveFragment.value = ""
                }
                TabCode.REGISTER_STEP3.NAME ->
                {
                    TabCode.REGISTER_STEP3.FRAGMENT = RegisterStep3.newInstance(binding.vm!!.wayBilNum.value, binding.vm!!.courier.value)
                    FragmentManager.move(parentView, TabCode.REGISTER_STEP3, RegisterMainFrame.viewId)
                    binding.vm!!.moveFragment.value = ""
                }
            }
        })
    }

    override fun onResume()
    {
        super.onResume()

        SopoLog.d(tag = TAG, msg = "OnResume")

        binding.customEtTrackNum.setOnClearListener(context)

        binding.customEtTrackNum.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER)
            {
                OtherUtil.hideKeyboardSoft(activity!!)
                binding.customEtTrackNum.etClearFocus()
            }
        }

        // 0922 kh 추가사항 - 클립보드에 저장되어있는 운송장 번호가 로컬에 등록된 택배가 있을 때, 안띄어주는 로직 추가
        ClipboardUtil.pasteClipboardText(SOPOApp.INSTANCE, parcelRepoImpl) {
            val isRegister = binding.vm?.wayBilNum?.value.isNullOrEmpty()

            if (!(it.isEmpty() || !isRegister))
            {
                binding.vm?.clipBoardWords?.value = it
            }
        }

    }

    companion object
    {
        fun newInstance(wayBilNum : String?, courier : CourierItem?, returnType : Int?) : RegisterStep1
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