package com.delivery.sopo.views.registers

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.database.room.RoomActivate
import com.delivery.sopo.databinding.RegisterStep1Binding
import com.delivery.sopo.enums.FragmentTypeEnum
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.repository.impl.CourierRepolmpl
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.util.ClipboardUtil
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomAlertMsg
import com.delivery.sopo.viewmodels.registesrs.RegisterStep1ViewModel
import com.delivery.sopo.views.main.MainView
import com.google.android.material.snackbar.Snackbar
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

        return binding.root
    }

    var callback: OnBackPressedCallback? = null

    override fun onAttach(context: Context)
    {
        super.onAttach(context)

        var pressedTime: Long = 0

        callback = object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                if (System.currentTimeMillis() - pressedTime > 2000)
                {
                    pressedTime = System.currentTimeMillis()
                    val snackbar = Snackbar.make(
                        parentView.binding.layoutMain,
                        "한번 더 누르시면 앱이 종료됩니다.",
                        2000
                    )
                    snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()

                    SopoLog.d("Register Step::1 BackPressListener = 종료를 위해 한번 더 클릭", null)
                }
                else
                {
                    SopoLog.d("Register Step::1 BackPressListener = 종료", null)
                    ActivityCompat.finishAffinity(activity!!)
                    System.exit(0)
                }
            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback!!)
    }

    override fun onDetach()
    {
        super.onDetach()

        callback!!.remove()
    }

    fun setObserve()
    {
        var pressedTime: Long = 0

        parentView.currentPage.observe(this, Observer {
            if (it != null && it == 0)
            {
                callback = object : OnBackPressedCallback(true)
                {
                    override fun handleOnBackPressed()
                    {
                        if (System.currentTimeMillis() - pressedTime > 2000)
                        {
                            pressedTime = System.currentTimeMillis()
                            val snackbar = Snackbar.make(
                                parentView.binding.layoutMain,
                                "한번 더 누르시면 앱이 종료됩니다.",
                                2000
                            )
                            snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()

                            SopoLog.d("Register Step::1 BackPressListener = 종료를 위해 한번 더 클릭", null)
                        }
                        else
                        {
                            SopoLog.d("Register Step::1 BackPressListener = 종료", null)
                            ActivityCompat.finishAffinity(activity!!)
                            System.exit(0)
                        }

                    }

                }

                requireActivity().onBackPressedDispatcher.addCallback(this, callback!!)
            }
        })

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
                FragmentTypeEnum.REGISTER_STEP2.NAME ->
                {
                    FragmentTypeEnum.REGISTER_STEP2.FRAGMENT =
                        RegisterStep2.newInstance(
                            binding.vm!!.waybilNum.value,
                            binding.vm!!.courier.value
                        )

                    FragmentManager.move(
                        activity!!,
                        FragmentTypeEnum.REGISTER_STEP2,
                        RegisterMainFrame.viewId
                    )
                    binding.vm?.moveFragment?.value = ""
                }
                FragmentTypeEnum.REGISTER_STEP3.NAME ->
                {
                    FragmentTypeEnum.REGISTER_STEP3.FRAGMENT =
                        RegisterStep3.newInstance(
                            binding.vm!!.waybilNum.value,
                            binding.vm!!.courier.value
                        )

                    FragmentManager.move(
                        activity!!,
                        FragmentTypeEnum.REGISTER_STEP3,
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

        SopoLog.d( tag = TAG, str = "OnResume")

        // 0922 kh 추가사항 - 클립보드에 저장되어있는 운송장 번호가 로컬에 등록된 택배가 있을 때, 안띄어주는 로직 추가
        ClipboardUtil.pasteClipboardText(SOPOApp.INSTANCE, parcelRepolmpl){
            val isRegister = binding.vm?.waybilNum?.value.isNullOrEmpty()

            if (!(it.isEmpty() || !isRegister))
            {
                binding.vm?.clipboardStr?.value = it
            }
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