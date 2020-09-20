package com.delivery.sopo.views.registers

import android.os.Bundle
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
import com.delivery.sopo.interfaces.OnMainBackPressListener
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.repository.CourierRepolmpl
import com.delivery.sopo.util.fun_util.ClipboardUtil
import com.delivery.sopo.util.ui_util.CustomAlertMsg
import com.delivery.sopo.util.ui_util.FragmentManager
import com.delivery.sopo.viewmodels.registesrs.RegisterStep1ViewModel
import com.delivery.sopo.views.MainView
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterStep1 : Fragment()
{
    private lateinit var parentView : MainView

    private lateinit var binding: RegisterStep1Binding
    private val registerStep1Vm: RegisterStep1ViewModel by viewModel()
    private val courierRepolmpl: CourierRepolmpl by inject()

    private var waybilNum: String? = null
    private var courier: CourierItem? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        if (arguments != null)
        {
            waybilNum = arguments!!.getString("waybilNum") ?: ""
            courier = arguments!!.getSerializable("courier") as CourierItem ?: null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
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

        parentView = activity as MainView

        parentView.setOnBackPressListener(object : OnMainBackPressListener{
            override fun onBackPressed()
            {
                Log.d("LOG.SOPO", "OnBackPressed")

                parentView.moveTaskToBack(true);						// 태스크를 백그라운드로 이동
                parentView.finishAndRemoveTask();						// 액티비티 종료 + 태스크 리스트에서 지우기
                android.os.Process.killProcess(android.os.Process.myPid());
            }

        })



        return binding.root
    }

    fun setObserve()
    {
        binding.vm?.waybilNum?.observe(this, Observer {
            if (it.isNotEmpty())
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
                    val result = RoomActivate.recommendAutoCourier(SOPOApp.INSTANCE, it, 1, courierRepolmpl)

                    if(result != null && result.size > 0)
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

        // todo 등록된 택배 운송장 번호와 비교해서 clipboard text와 같거나 운송장 번호 et에 등록 중이면 아래 로직 생략
        val text = ClipboardUtil.pasteClipboardText(SOPOApp.INSTANCE)

        val isRegister = binding.vm?.waybilNum?.value.isNullOrEmpty()

        if (!(text.isEmpty() || !isRegister))
        {
            binding.vm?.clipboardStr?.value = text
        }
    }

    companion object
    {
        fun newInstance(waybilNum: String?, courier: CourierItem?): RegisterStep1
        {
            val registerStep1 = RegisterStep1()

            val args = Bundle()

            args.putString("waybilNum", waybilNum)
            args.putSerializable("courier", courier)

            registerStep1.arguments = args
            return registerStep1
        }
    }
}