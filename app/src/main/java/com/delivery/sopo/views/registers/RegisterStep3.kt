package com.delivery.sopo.views.registers

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.databinding.RegisterStep3Binding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.models.BindView
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.models.TestResult
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.viewmodels.registesrs.RegisterStep3ViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.main.MainView
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterStep3: Fragment()
{
    private lateinit var parentView: MainView

    private lateinit var binding: RegisterStep3Binding
    private val registerStep3Vm: RegisterStep3ViewModel by viewModel()

    private val userRepoImpl: UserRepoImpl by inject()

    private var wayBilNum: String? = null
    private var courier: CourierItem? = null

    private var progressBar: CustomProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        wayBilNum = arguments?.getString("wayBilNum") ?: ""
        courier = arguments?.getSerializable("courier") as CourierItem ?: null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        parentView = activity as MainView

        BindView<RegisterStep3Binding>(requireActivity(), inflater, container, R.layout.register_step3).run {
            binding = bindView()
            setViewModel(registerStep3Vm)
            setExecutePendingBindings()
        }

        progressBar = CustomProgressBar(requireActivity())

        binding.vm!!.wayBilNum.value = wayBilNum
        binding.vm!!.courier.value = courier

        setObserve()

        return binding.root
    }

    private fun setObserve()
    {
        parentView.currentPage.observe(this, Observer { currentPage ->

            if (currentPage == null) return@Observer

            if (currentPage == 0)
            {
                callback = object: OnBackPressedCallback(true)
                {
                    override fun handleOnBackPressed()
                    {
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                }

                requireActivity().onBackPressedDispatcher.addCallback(this, callback!!)
            }
        })

        binding.vm!!.isProgress.observe(this@RegisterStep3, Observer {isLoading ->
            if(isLoading == null) return@Observer

            if(progressBar == null)
            {
                progressBar = CustomProgressBar(requireActivity())
            }

            if(isLoading)
            {
                progressBar?.onStartDialog()
                return@Observer
            }

            progressBar?.onCloseDialog()?:return@Observer
        })

        binding.vm!!.isRevise.observe(this, Observer {
            if (it != null && it)
            {
                TabCode.REGISTER_STEP1.FRAGMENT = RegisterStep1.newInstance(wayBilNum, courier, 0)

                FragmentManager.initFragment(
                    activity = activity!!, viewId = RegisterMainFrame.viewId, currentFragment = this@RegisterStep3, nextFragment = TabCode.REGISTER_STEP1.FRAGMENT, nextFragmentTag = TabCode.REGISTER_STEP1.NAME
                )

                binding.vm!!.isRevise.call()
            }
        })

        binding.vm!!.result.observe(this, Observer { result ->
            when (result)
            {
                is TestResult.SuccessResult<*> ->
                {
                    if (userRepoImpl.getTopic().isEmpty())
                    {
                        FirebaseRepository.subscribedToTopicInFCM { s, e ->
                            if (s != null) SopoLog.d(msg = "구독 성공 >>> ${s.successMsg}")

                            return@subscribedToTopicInFCM
                        }
                    }

                    FirebaseRepository.unsubscribedToTopicInFCM { s, e ->
                        if (e != null)
                        {
                            SopoLog.e(msg = "구독 해지 실패 >>>${e.errorMsg}")
                            return@unsubscribedToTopicInFCM
                        }
                        if (s != null)
                        {
                            SopoLog.d(msg = "구독 해지 성공 >>> ${s.successMsg}")

                            FirebaseRepository.subscribedToTopicInFCM { s, e ->
                                if (e != null) SopoLog.e(msg = "구독 실패 >>> ${e.errorMsg}")
                                if (s != null) SopoLog.d(msg = "구독 성공 >>> ${s.successMsg}")
                            }
                        }
                    }

                    TabCode.REGISTER_STEP1.FRAGMENT = RegisterStep1.newInstance(null, null, 1)
                    FragmentManager.initFragment(
                        activity = requireActivity(), viewId = RegisterMainFrame.viewId, currentFragment = this@RegisterStep3, nextFragment = TabCode.REGISTER_STEP1.FRAGMENT, nextFragmentTag = TabCode.REGISTER_STEP1.NAME
                    )
                }
                is TestResult.ErrorResult<*> ->
                {
                    GeneralDialog(
                        act = activity!!, title = "오류", msg = result.errorMsg, detailMsg = null, rHandler = Pair(first = "네", second = { it ->
                            it.dismiss()
                        })
                    ).show(activity!!.supportFragmentManager, "tag")
                }
            }
        })
    }

    var callback: OnBackPressedCallback? = null

    override fun onAttach(context: Context)
    {
        super.onAttach(context)

        callback = object: OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                SopoLog.d(msg = "Register Step::3 BackPressListener")
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