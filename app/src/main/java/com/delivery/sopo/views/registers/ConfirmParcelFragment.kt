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
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.firebase.FirebaseNetwork
import com.delivery.sopo.models.BindView
import com.delivery.sopo.models.CarrierDTO
import com.delivery.sopo.models.TestResult
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.databinding.FragmentConfirmParcelBinding
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

    private var waybillNum: String? = null
    private var carrierDTO: CarrierDTO? = null

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

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        waybillNum = arguments?.getString("waybillNum") ?: ""
        carrierDTO = arguments?.getSerializable("carrier") as CarrierDTO ?: null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        parentView = activity as MainView

        BindView<FragmentConfirmParcelBinding>(requireActivity(), inflater, container,
                                       R.layout.fragment_confirm_parcel).run {
            binding = bindView()
            vm = this@ConfirmParcelFragment.vm
            setExecutePendingBindings()
        }

        binding.vm!!.waybillNum.value = waybillNum
        binding.vm!!.carrier.value = carrierDTO

        setObserve()

        return binding.root
    }

    private fun setObserve()
    {
        parentView.currentPage.observe(this, Observer { currentPage ->
            if(currentPage == null || currentPage != 0) return@Observer

            requireActivity().onBackPressedDispatcher.addCallback(this, callback ?: throw Exception("BackPressedClick is null"))
        })

        binding.vm!!.isProgress.observe(this, Observer { isProgress ->
            if(isProgress == null) return@Observer

            if(progressBar == null)
            {
                progressBar = CustomProgressBar(parentView)
            }

            progressBar?.onStartProgress(isProgress) { isDismiss ->
                if(isDismiss) progressBar = null
            }
        })

        binding.vm!!.isRevise.observe(this, Observer {
            if(it != null && it)
            {
                TabCode.REGISTER_STEP1.FRAGMENT =
                    InputParcelFragment.newInstance(waybillNum, carrierDTO, 0)

                FragmentManager.initFragment(activity = activity!!,
                                             viewId = RegisterMainFragment.layoutId,
                                             currentFragment = this@ConfirmParcelFragment,
                                             nextFragment = TabCode.REGISTER_STEP1.FRAGMENT,
                                             nextFragmentTag = TabCode.REGISTER_STEP1.NAME)

                binding.vm!!.isRevise.call()
            }
        })

        binding.vm!!.result.observe(this, Observer { result ->
            when(result)
            {
                is TestResult.SuccessResult<*> ->
                {
                    if(userLocalRepository.getTopic().isNotEmpty())
                    {
                        FirebaseNetwork.unsubscribedToTopicInFCM()
                    }

                    FirebaseNetwork.subscribedToTopicInFCM()

                    TabCode.REGISTER_STEP1.FRAGMENT = InputParcelFragment.newInstance(null, null, 1)
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
        fun newInstance(waybillNum: String?, carrierDTO: CarrierDTO?): ConfirmParcelFragment
        {
            val registerStep3 = ConfirmParcelFragment()

            val args = Bundle()

            args.putString("waybillNum", waybillNum)
            args.putSerializable("carrier", carrierDTO)

            registerStep3.arguments = args
            return registerStep3
        }
    }
}