package com.delivery.sopo.views.menus

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.databinding.FragmentSettingBinding
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.enums.MenuEnum
import com.delivery.sopo.extensions.launchActivitiy
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.TimeCountRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.viewmodels.factory.MenuViewModelFactory
import com.delivery.sopo.viewmodels.menus.MenuViewModel
import com.delivery.sopo.viewmodels.menus.SettingViewModel
import com.delivery.sopo.views.dialog.SelectNotifyKindDialog
import com.delivery.sopo.views.main.MainView
import kotlinx.android.synthetic.main.fragment_setting.view.*
import kotlinx.android.synthetic.main.menu_view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class SettingFragment : Fragment()
{
    private val settingVM: SettingViewModel by viewModel()
    private lateinit var binding: FragmentSettingBinding
    private val userRepoImpl: UserRepoImpl by inject()
    private val parcelRepoImpl: ParcelRepoImpl by inject()
    private val timeCountRepoImpl: TimeCountRepoImpl by inject()
    private lateinit var parentView: MainView

    private val menuVm: MenuViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            MenuViewModelFactory(userRepoImpl, parcelRepoImpl, timeCountRepoImpl)
        ).get(MenuViewModel::class.java)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = FragmentSettingBinding.inflate(inflater, container, false)

        parentView = activity as MainView

        viewBinding()
        setObserver()
        setListener()

        lifecycle.addObserver(settingVM)

        return binding.root
    }

    private fun viewBinding()
    {
        binding.vm = settingVM
        binding.lifecycleOwner = this
        binding.executePendingBindings() // 즉 바인딩
    }

    private fun setListener()
    {
        binding.root.constraint_how_to_set_notify.setOnClickListener {
            SelectNotifyKindDialog(this.requireActivity()).show(
                requireActivity().supportFragmentManager,
                "SelectNotifyKindDialog"
            )
        }
        binding.root.linear_set_no_disturbance_time.setOnClickListener {
            gotoSetOfNotDisturbTimeView()
        }
        binding.root.tv_change_password.setOnClickListener {
            activity?.launchActivitiy<LockScreenView> {
                putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.SET)
            }
        }

    }

    private fun gotoSetOfNotDisturbTimeView()
    {
        menuVm.pushView(MenuEnum.NOT_DISTURB)
    }

    fun setObserver()
    {
        settingVM.showSetPassword.observe(this, Observer {
            if (it)
            {
                activity?.launchActivitiy<LockScreenView> {
                    putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.SET)
                }
            }
            else
            {
            }
        })
    }
}