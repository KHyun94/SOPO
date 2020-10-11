package com.delivery.sopo.views.registers

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.database.room.RoomActivate
import com.delivery.sopo.databinding.RegisterStep1Binding
import com.delivery.sopo.enums.FragmentType
import com.delivery.sopo.interfaces.listener.OnMainBackPressListener
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.repository.impl.CourierRepolmpl
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.util.ClipboardUtil
import com.delivery.sopo.util.ui_util.CustomAlertMsg
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.viewmodels.registesrs.RegisterStep1ViewModel
import com.delivery.sopo.views.main.MainView
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterStep1 : Fragment()
{
    private val TAG = "LOG.SOPO"

    private lateinit var parentView: MainView

    private lateinit var binding: RegisterStep1Binding
    private val registerStep1Vm: RegisterStep1ViewModel by viewModel()
    private val courierRepolmpl: CourierRepolmpl by inject()
    private val parcelRepolmpl: ParcelRepoImpl by inject()

    private var waybilNum: String? = null
    private var courier: CourierItem? = null
    private var returnType: Int? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        if (arguments != null)
        {
            Log.d(TAG, "")

            arguments.run {
                waybilNum = this?.getString("waybilNum") ?: ""
                courier = this?.getSerializable("courier") as CourierItem?
                returnType = this?.getInt("returnType") ?: 0
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        parentView = activity as MainView

        binding = DataBindingUtil.inflate(inflater, R.layout.register_step1, container, false)
        binding.vm = registerStep1Vm
        binding.lifecycleOwner = this

        setObserve()

        if (waybilNum != null && waybilNum!!.isNotEmpty())
        {
            binding.vm!!.waybilNum.value = waybilNum
        }

        if (courier != null)
        {
            binding.vm!!.courier.value = courier
        }

        // 0922 kh 등록 완료 시 조회탭으로 이동
        if (returnType != null && returnType == 1)
        {
            val handler = Handler()
            handler.post {
                parentView.onCompleteRegister()
            }
        }

        parentView.setOnBackPressListener(object : OnMainBackPressListener
        {
            override fun onBackPressed()
            {
                Log.d("LOG.SOPO", "OnBackPressed")

                parentView.moveTaskToBack(true);                        // 태스크를 백그라운드로 이동
                parentView.finishAndRemoveTask();                        // 액티비티 종료 + 태스크 리스트에서 지우기
                android.os.Process.killProcess(android.os.Process.myPid());
            }

        })



        return binding.root
    }

    fun setObserve()
    {
        binding.vm?.waybilNum?.observe(this, Observer {
            if (it != null && it.isNotEmpty())
            {
                binding.vm?.clipboardStr?.value = ""

                if (it.isNotEmpty())
                {
                    binding.vm!!.waybilNoStatusType.value = 1

                }
                else
                {
                    binding.vm!!.waybilNoStatusType.value = -1
                }

                if (it.length > 8)
                {
                    val result =
                        RoomActivate.recommendAutoCourier(SOPOApp.INSTANCE, it, 1, courierRepolmpl)

                    if (result != null && result.size > 0)
                    {
                        binding.vm!!.courier.postValue(result[0])
                    }


                }
                else
                {
                    binding.vm!!.courier.value = null
                }
            }
        })

        binding.vm!!.errorMsg.observe(this, Observer {
            if (!it.isNullOrEmpty())
            {
                CustomAlertMsg.floatingUpperSnackBAr(this.context!!, it, true)
                binding.vm!!.errorMsg.value = ""
            }
        })

        binding.vm?.moveFragment?.observe(this, Observer {
            when (it)
            {
                FragmentType.REGISTER_STEP2.NAME ->
                {
                    FragmentType.REGISTER_STEP2.FRAGMENT =
                        RegisterStep2.newInstance(
                            binding.vm!!.waybilNum.value,
                            binding.vm!!.courier.value
                        )

                    FragmentManager.move(
                        activity!!,
                        FragmentType.REGISTER_STEP2,
                        RegisterMainFrame.viewId
                    )
                    binding.vm?.moveFragment?.value = ""
                }
                FragmentType.REGISTER_STEP3.NAME ->
                {
                    FragmentType.REGISTER_STEP3.FRAGMENT =
                        RegisterStep3.newInstance(
                            binding.vm!!.waybilNum.value,
                            binding.vm!!.courier.value
                        )

                    FragmentManager.move(
                        activity!!,
                        FragmentType.REGISTER_STEP3,
                        RegisterMainFrame.viewId
                    )
                    binding.vm?.moveFragment?.value = ""
                }
            }
        })

        binding.vm?.courier?.observe(this, Observer {
            val courier = it
        })
    }

    override fun onResume()
    {
        super.onResume()

        // 0922 kh 추가사항 - 클립보드에 저장되어있는 운송장 번호가 로컬에 등록된 택배가 있을 때, 안띄어주는 로직 추가
        val text = ClipboardUtil.pasteClipboardText(SOPOApp.INSTANCE, parcelRepolmpl)

        val isRegister = binding.vm?.waybilNum?.value.isNullOrEmpty()

        if (!(text.isEmpty() || !isRegister))
        {
            binding.vm?.clipboardStr?.value = text
        }
    }

    companion object
    {
        fun newInstance(waybilNum: String?, courier: CourierItem?, returnType: Int?): RegisterStep1
        {
            val registerStep1 = RegisterStep1()

            val args = Bundle()

            args.putString("waybilNum", waybilNum)
            args.putSerializable("courier", courier)
            // 다른 프래그먼트에서 돌아왔을 때 분기 처리
            // 0: Default 1: Success To Register
            args.putInt("returnType", returnType ?: 0)

            registerStep1.arguments = args
            return registerStep1
        }
    }
}