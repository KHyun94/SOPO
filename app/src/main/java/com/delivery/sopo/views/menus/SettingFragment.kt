package com.delivery.sopo.views.menus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.FragmentSettingBinding
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.launchActivitiy
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.menus.MenuMainFragment
import com.delivery.sopo.viewmodels.menus.SettingViewModel
import com.delivery.sopo.views.dialog.SelectNotifyKindDialog
import com.delivery.sopo.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel


class SettingFragment: BaseFragment<FragmentSettingBinding, SettingViewModel>()
{
    override val vm: SettingViewModel by viewModel()
    override val layoutRes: Int = R.layout.fragment_setting
    override val mainLayout: View by lazy { binding.constraintMainSetting }
    private val parentView: MainView by lazy { activity as MainView }

    override fun setBeforeBinding()
    {
        super.setBeforeBinding()

        useCommonBackPressListener(isUseCommon = true)

        onSOPOBackPressedListener = object: OnSOPOBackPressEvent(true)
        {
            override fun onBackPressed()
            {
                super.onBackPressed()
                TabCode.MY_MENU_MAIN.FRAGMENT = MenuFragment.newInstance()
                FragmentManager.move(requireActivity(), TabCode.MY_MENU_MAIN, MenuMainFragment.viewId)
            }
        }
    }

    override fun setObserve()
    {
        super.setObserve()

        activity ?: return
        parentView.currentPage.observe(this) {
            if(it != 2) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.navigator.observe(requireActivity(), Observer { navigator ->
            SopoLog.d("navigator[$navigator]")
            when(navigator)
            {
                NavigatorConst.TO_NOT_DISTURB ->
                {
                    val intent = Intent(parentView, NotDisturbTimeView::class.java)
                    startActivity(intent)
                }
                NavigatorConst.TO_SET_NOTIFY_OPTION ->
                {
                    SelectNotifyKindDialog().show(requireActivity().supportFragmentManager, "SelectNotifyKindDialog")
                }
                NavigatorConst.TO_UPDATE_APP_PASSWORD ->
                {
                    activity?.launchActivitiy<LockScreenView> {
                        putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.SET)
                    }
                }
            }
        })

        vm.showSetPassword.observe(requireActivity(), Observer {
            if(it)
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

    companion object
    {
        fun newInstance(): SettingFragment
        {
            return SettingFragment()
        }
    }
}