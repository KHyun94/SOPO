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
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.databinding.RegisterStep3Binding
import com.delivery.sopo.enums.FragmentType
import com.delivery.sopo.interfaces.OnMainBackPressListener
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.util.ui_util.FragmentManager
import com.delivery.sopo.util.ui_util.GeneralDialog
import com.delivery.sopo.viewmodels.registesrs.RegisterStep3ViewModel
import com.delivery.sopo.views.MainView
import kotlinx.android.synthetic.main.intro_view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterStep3 : Fragment()
{
    private val TAG = "LOG.SOPO"
    private lateinit var parentView : MainView

    private lateinit var binding: RegisterStep3Binding
    private val registerStep3Vm: RegisterStep3ViewModel by viewModel()

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
        parentView = activity as MainView

        binding = DataBindingUtil.inflate(inflater, R.layout.register_step3, container, false)
        binding.vm = registerStep3Vm
        binding.lifecycleOwner = this

        if (waybilNum != null && waybilNum!!.isNotEmpty())
        {
            binding.vm!!.waybilNum.value = waybilNum
        }

        if (courier != null)
        {
            binding.vm!!.courier.value = courier
        }

        setObserve()

        parentView.setOnBackPressListener(object : OnMainBackPressListener
        {
            override fun onBackPressed()
            {
                Log.d("LOG.SOPO", "OnBackPressed task 2")
                FragmentManager.remove(activity!!)
            }

        })

        return binding.root
    }

    private fun setObserve()
    {
        binding.vm!!.isRevise.observe(this, Observer {
            if (it != null && it)
            {
                FragmentType.REGISTER_STEP1.FRAGMENT = RegisterStep1.newInstance(waybilNum, courier, 0)

                FragmentManager.initFragment(
                    activity = activity!!,
                    viewId = RegisterMainFrame.viewId,
                    currentFragment = this@RegisterStep3,
                    nextFragment = FragmentType.REGISTER_STEP1.FRAGMENT,
                    nextFragmentTag = FragmentType.REGISTER_STEP1.NAME
                )

                binding.vm!!.isRevise.call()
            }
        })

        binding.vm!!.validate.observe(this, Observer {
            if(it != null)
            {
                if(it.result)
                {
                    Log.d(TAG, "등록 성공 $it")

                    FragmentType.REGISTER_STEP1.FRAGMENT = RegisterStep1.newInstance(null, null, 1)

                    FragmentManager.initFragment(
                        activity = activity!!,
                        viewId = RegisterMainFrame.viewId,
                        currentFragment = this@RegisterStep3,
                        nextFragment = FragmentType.REGISTER_STEP1.FRAGMENT,
                        nextFragmentTag = FragmentType.REGISTER_STEP1.NAME
                    )
                }
                else
                {
                    when(it.showType)
                    {
                        InfoConst.NON_SHOW ->
                        {

                        }
                        InfoConst.CUSTOM_DIALOG ->
                        {
                            GeneralDialog(
                                act = activity!!,
                                title = "오류",
                                msg = it.msg,
                                detailMsg = null,
                                rHandler = Pair(
                                    first = "네",
                                    second = { it ->
                                        it.dismiss()
                                    })
                            ).show(activity!!.supportFragmentManager, "tag")
                        }
                        InfoConst.ERROR_ACTIVITY ->
                        {

                        }
                    }

                }

            }

        })
    }

    companion object
    {
        fun newInstance(waybilNum: String?, courier: CourierItem?): RegisterStep3
        {
            val registerStep3 = RegisterStep3()

            val args = Bundle()

            args.putString("waybilNum", waybilNum)
            args.putSerializable("courier", courier)

            registerStep3.arguments = args
            return registerStep3
        }
    }
}