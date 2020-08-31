package com.delivery.sopo.views.menus

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentSettingBinding
import com.delivery.sopo.extentions.launchActivitiy
import com.delivery.sopo.util.ui_util.SelectNotifyKindDialog
import com.delivery.sopo.viewmodels.menus.SettingViewModel
import kotlinx.android.synthetic.main.fragment_setting.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class SettingFragment : Fragment(){

    private val settingVM: SettingViewModel by viewModel()
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"
    private lateinit var binding: FragmentSettingBinding

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        viewBinding()
        setObserver()

        lifecycle.addObserver(settingVM)

        return binding.root
    }

    private fun viewBinding() {
        binding.vm = settingVM
        binding.lifecycleOwner = this
        binding.executePendingBindings() // 즉 바인딩
    }

    fun setObserver(){
        binding.vm!!.isSecuritySetting.observe(this, Observer {
            if (it)
            {
                toggleBtn.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_toggle_on
                )
                tv_lockStatus.text = "설정하기"
                linear_guideWord.visibility = VISIBLE
                tv_changePassword.visibility = VISIBLE

                activity?.launchActivitiy<LockScreenView>()
            }
            else
            {
                toggleBtn.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_toggle_off
                )
                tv_lockStatus.text = "설정 안 함"
                linear_guideWord.visibility = GONE
                tv_changePassword.visibility = GONE
            }
        })

        binding.vm!!.testval.observe(this, Observer {
            if (it > 0){
                showtestDialog()
            }
        })
    }

    private fun showtestDialog(){

//        NotDisturbTimeDialog(act = requireActivity()).show(requireActivity().supportFragmentManager, "NotDisturbTimeDialog")
        SelectNotifyKindDialog(act = requireActivity()).show(requireActivity().supportFragmentManager, "SelectNotifyKindDialog")
    }
}