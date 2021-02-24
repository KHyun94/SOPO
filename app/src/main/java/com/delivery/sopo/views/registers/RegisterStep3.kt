package com.delivery.sopo.views.registers

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.databinding.RegisterStep3Binding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.workmanager.SOPOWorkManager
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.viewmodels.registesrs.RegisterStep3ViewModel
import com.delivery.sopo.views.main.MainView
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterStep3 : Fragment()
{
    private val TAG = "LOG.SOPO"
    private lateinit var parentView : MainView

    private lateinit var binding: RegisterStep3Binding
    private val registerStep3Vm: RegisterStep3ViewModel by viewModel()
    private val userRepoImpl : UserRepoImpl by inject()

    private var wayBilNum: String? = null
    private var courier: CourierItem? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        if (arguments != null)
        {
            wayBilNum = arguments!!.getString("wayBilNum") ?: ""
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

        if (wayBilNum != null && wayBilNum!!.isNotEmpty())
        {
            binding.vm!!.wayBilNum.value = wayBilNum
        }

        if (courier != null)
        {
            binding.vm!!.courier.value = courier
        }

        setObserve()

        return binding.root
    }

    private fun setObserve()
    {
        parentView.currentPage.observe(this, Observer {
            if(it != null && it == 0)
            {
                callback = object : OnBackPressedCallback(true){
                    override fun handleOnBackPressed()
                    {
                        SopoLog.d(tag = TAG, msg = "Register Step::3 BackPressListener")
                        requireActivity().supportFragmentManager.popBackStack()
                    }

                }

                requireActivity().onBackPressedDispatcher.addCallback(this, callback!!)
            }
        })

        binding.vm!!.isRevise.observe(this, Observer {
            if (it != null && it)
            {
                TabCode.REGISTER_STEP1.FRAGMENT = RegisterStep1.newInstance(wayBilNum, courier, 0)

                FragmentManager.initFragment(
                    activity = activity!!,
                    viewId = RegisterMainFrame.viewId,
                    currentFragment = this@RegisterStep3,
                    nextFragment = TabCode.REGISTER_STEP1.FRAGMENT,
                    nextFragmentTag = TabCode.REGISTER_STEP1.NAME
                )

                binding.vm!!.isRevise.call()
            }
        })

        binding.vm!!.validate.observe(this, Observer {
            if(it != null)
            {
                if(it.result)
                {
                    SopoLog.d(tag = TAG, msg = "등록 성공 $it")

                    if(userRepoImpl.getTopic().isEmpty())
                    {
                        FirebaseRepository.subscribedToTopicInFCM{ s, e ->
                            if(e!=null) SopoLog.e(tag = TAG, msg ="구독 실패 >>> ${e.errorMsg}")
                            if(s!= null) SopoLog.d(tag = TAG, msg = "구독 성공 >>> ${s.successMsg}")
                        }
                    }
                    else
                    {
                        FirebaseRepository.unsubscribedToTopicInFCM{s, e->
                            if(e!=null) SopoLog.e(tag = TAG, msg ="구독 해지 실패 >>>${e.errorMsg}")
                            if(s!= null)
                            {
                                SopoLog.d(tag = TAG, msg = "구독 해지 성공 >>> ${s.successMsg}")

                                FirebaseRepository.subscribedToTopicInFCM{ s, e ->
                                    if(e!=null) SopoLog.e(tag = TAG, msg ="구독 실패 >>> ${e.errorMsg}")
                                    if(s!= null) SopoLog.d(tag = TAG, msg = "구독 성공 >>> ${s.successMsg}")
                                }
                            }
                        }
                    }

                    TabCode.REGISTER_STEP1.FRAGMENT = RegisterStep1.newInstance(null, null, 1)
                    FragmentManager.initFragment(
                        activity = activity!!,
                        viewId = RegisterMainFrame.viewId,
                        currentFragment = this@RegisterStep3,
                        nextFragment = TabCode.REGISTER_STEP1.FRAGMENT,
                        nextFragmentTag = TabCode.REGISTER_STEP1.NAME
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
    var callback : OnBackPressedCallback? = null

    override fun onAttach(context: Context)
    {
        super.onAttach(context)

        callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed()
            {
                SopoLog.d(tag = TAG, msg = "Register Step::3 BackPressListener")
                requireActivity().supportFragmentManager.popBackStack()
            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback!!)
    }

    override fun onDetach()
    {
        super.onDetach()
        callback!!.remove()
    }
    companion object
    {
        fun newInstance(wayBilNum: String?, courier: CourierItem?): RegisterStep3
        {
            val registerStep3 = RegisterStep3()

            val args = Bundle()

            args.putString("wayBilNum", wayBilNum)
            args.putSerializable("courier", courier)

            registerStep3.arguments = args
            return registerStep3
        }
    }
}