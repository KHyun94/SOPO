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
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.firebase.FirebaseNetwork
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.databinding.FragmentConfirmParcelBinding
import com.delivery.sopo.models.*
import com.delivery.sopo.models.mapper.CarrierMapper
import com.delivery.sopo.util.CarrierUtil
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.viewmodels.registesrs.ConfirmParcelViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.main.MainView
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConfirmParcelFragment: Fragment()
{
    private lateinit var parentView: MainView

    private lateinit var binding: FragmentConfirmParcelBinding
    private val vm: ConfirmParcelViewModel by viewModel()

    private val userLocalRepository: UserLocalRepository by inject()

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

        requireActivity().onBackPressedDispatcher.addCallback(this, callback ?: throw Exception(
            "BackPressedClick is null"))
    }

    private fun receiveBundleData(){
        arguments?.let { bundle ->
            registerDTO = bundle.getSerializable(RegisterMainFragment.REGISTER_INFO) as ParcelRegisterDTO
            SopoLog.d("receive bundle data >>> ${registerDTO.toString()}")
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

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_confirm_parcel, container,  false)

        binding.vm = vm

        registerDTO.waybillNum?.let { waybillNo ->
            binding.vm!!.waybillNum.value = waybillNo
            SopoLog.d("운송장번호 >>> ${binding.vm!!.waybillNum.value}")
        }
        registerDTO.carrier?.let { carrierEnum ->
            val carrierDTO = CarrierMapper.enumToObject(carrierEnum)
            vm.carrier.value = carrierDTO
            SopoLog.d("택배사 >>> $carrierDTO")
        }
        registerDTO.alias?.let { alias ->
            SopoLog.d("별칭 >>> $alias")
            vm.alias.postValue(alias)
        }

        setObserve()

        SopoLog.d("""
            confirm parcel data >>>
            ${binding.vm?.waybillNum?.value}
            ${binding.vm?.carrier?.value?.toString()}
        """.trimIndent())

        return binding.root
    }

    private fun setObserve()
    {
        parentView.currentPage.observe(this, Observer { currentPage ->
            if(currentPage == null || currentPage != 0) return@Observer

            requireActivity().onBackPressedDispatcher.addCallback(this, callback ?: throw Exception("BackPressedClick is null"))
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

        vm.isRevise.observe(this, Observer {
            if(it != null && it)
            {
                TabCode.REGISTER_STEP1.FRAGMENT =
                    InputParcelFragment.newInstance(registerDTO, 0)

                FragmentManager.initFragment(activity = activity!!,
                                             viewId = RegisterMainFragment.layoutId,
                                             currentFragment = this@ConfirmParcelFragment,
                                             nextFragment = TabCode.REGISTER_STEP1.FRAGMENT,
                                             nextFragmentTag = TabCode.REGISTER_STEP1.NAME)

                binding.vm!!.isRevise.call()
            }
        })

        vm.result.observe(this, Observer { result ->
            when(result)
            {
                is TestResult.SuccessResult<*> ->
                {
                    if(userLocalRepository.getTopic().isNotEmpty())
                    {
                        FirebaseNetwork.unsubscribedToTopicInFCM()
                    }

                    FirebaseNetwork.subscribedToTopicInFCM()

                    TabCode.REGISTER_STEP1.FRAGMENT = InputParcelFragment.newInstance(registerDTO = null, returnType = 1)
                    FragmentManager.initFragment(activity = requireActivity(),
                                                 viewId = RegisterMainFragment.layoutId,
                                                 currentFragment = this@ConfirmParcelFragment,
                                                 nextFragment = TabCode.REGISTER_STEP1.FRAGMENT,
                                                 nextFragmentTag = TabCode.REGISTER_STEP1.NAME)
                }
                is TestResult.ErrorResult<*> ->
                {
                    GeneralDialog(act = activity!!, title = "오류", msg = result.errorMsg,
                                  detailMsg = null, rHandler = Pair(first = "네", second = { it ->
                            it.dismiss()
                        })).show(activity!!.supportFragmentManager, "tag")
                }
            }
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
                putSerializable(RegisterMainFragment.REGISTER_INFO, registerDTO)
            }

            return ConfirmParcelFragment().apply {
                arguments = args
            }
        }
    }
}