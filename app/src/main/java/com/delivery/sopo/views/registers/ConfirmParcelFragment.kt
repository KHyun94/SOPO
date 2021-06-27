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
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.firebase.FirebaseNetwork
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.databinding.FragmentConfirmParcelBinding
import com.delivery.sopo.models.*
import com.delivery.sopo.models.mapper.CarrierMapper
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.viewmodels.registesrs.ConfirmParcelViewModel
import com.delivery.sopo.views.main.MainView
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConfirmParcelFragment: Fragment()
{
    private lateinit var parentView: MainView

    private lateinit var binding: FragmentConfirmParcelBinding
    private val vm: ConfirmParcelViewModel by viewModel()

    private val userLocalRepo: UserLocalRepository by inject()

    private lateinit var registerDTO: ParcelRegisterDTO

    private var progressBar: CustomProgressBar? = null

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

        requireActivity().onBackPressedDispatcher.addCallback(this, callback ?: throw Exception("BackPressedClick is null"))
    }

    // TODO 값이 없다면 이전 화면으로 이동
    private fun receiveBundleData()
    {
        arguments?.let { bundle ->
            registerDTO =
                bundle.getSerializable(RegisterMainFrame.REGISTER_INFO) as ParcelRegisterDTO
            SopoLog.d("receive bundle data >>> ${registerDTO.toString()}")
        }

        registerDTO.waybillNum?.let { waybillNo ->
            vm.waybillNum.value = waybillNo
        }

        registerDTO.carrier?.let { carrierEnum ->
            vm.carrier.value = CarrierMapper.enumToObject(carrierEnum)
        }
        registerDTO.alias?.let { alias ->
            SopoLog.d("별칭 >>> $alias")
            vm.alias.value = alias
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        receiveBundleData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        parentView = activity as MainView
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_confirm_parcel, container, false)
        binding.vm = vm

        setObserve()

        return binding.root
    }

    private fun setObserve()
    {
        parentView.currentPage.observe(this, Observer { currentPage ->
            if(currentPage == null || currentPage != 0) return@Observer

            requireActivity().onBackPressedDispatcher.addCallback(this, callback ?: throw Exception(
                "BackPressedClick is null"))
        })

        vm.isProgress.observe(this, Observer { isProgress ->
            if(isProgress == null) return@Observer

            if(progressBar == null)
            {
                progressBar = CustomProgressBar(parentView)
            }

            progressBar?.onStartProgress(isProgress) { isDismiss ->
                if(isDismiss) progressBar = null
            }
        })

        vm.navigator.observe(this, Observer { navigator ->
            TabCode.REGISTER_INPUT.FRAGMENT  = when(navigator)
            {
                NavigatorConst.TO_REGISTER_INIT ->
                {
                    InputParcelFragment.newInstance(null, RegisterMainFrame.REGISTER_PROCESS_RESET)
                }
                NavigatorConst.TO_REGISTER_REVISE ->
                {
                    InputParcelFragment.newInstance(registerDTO, RegisterMainFrame.REGISTER_PROCESS_RESET)
                }
                NavigatorConst.TO_REGISTER_SUCCESS ->
                {
                    if(userLocalRepo.getTopic().isNotEmpty())
                    {
                        FirebaseNetwork.unsubscribedToTopicInFCM()
                    }

                    FirebaseNetwork.subscribedToTopicInFCM()

                    InputParcelFragment.newInstance(null, RegisterMainFrame.REGISTER_PROCESS_SUCCESS)
                }
                else -> throw Exception("NOT SUPPORT REGISTER TYPE")
            }

            FragmentManager.initFragment(activity = requireActivity(),
                                         viewId = RegisterMainFrame.viewId,
                                         currentFragment = this@ConfirmParcelFragment,
                                         nextFragment = TabCode.REGISTER_INPUT.FRAGMENT,
                                         nextFragmentTag = TabCode.REGISTER_INPUT.NAME)

        })

        vm.result.observe(this, Observer { res ->

//            when(res.displayType)

        })
    }

    override fun onDetach()
    {
        super.onDetach()
        callback?.remove()
    }

    companion object
    {
        fun newInstance(registerDTO: ParcelRegisterDTO?): ConfirmParcelFragment
        {
            val args = Bundle().apply {
                putSerializable(RegisterMainFrame.REGISTER_INFO, registerDTO)
            }

            return ConfirmParcelFragment().apply {
                arguments = args
            }
        }
    }
}