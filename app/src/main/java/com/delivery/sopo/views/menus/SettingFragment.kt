package com.delivery.sopo.views.menus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.FragmentSettingBinding
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.launchActivitiy
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.menus.SettingViewModel
import com.delivery.sopo.views.dialog.SelectNotifyKindDialog
import com.delivery.sopo.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel


class SettingFragment : Fragment()
{
    private val vm: SettingViewModel by viewModel()
    private lateinit var binding: FragmentSettingBinding
    private lateinit var parentView: MainView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
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

        return binding.root
    }

    private fun viewBinding()
    {
        binding.vm = vm
        binding.lifecycleOwner = this
    }

    fun setObserver()
    {
        vm.navigator.observe(this, Observer { navigator ->
            SopoLog.d("navigator[$navigator]")
            when(navigator){
                NavigatorConst.TO_NOT_DISTURB ->
                {
                    val intent = Intent(parentView, NotDisturbTimeView::class.java)
                    startActivity(intent)
                }
                NavigatorConst.TO_SET_NOTIFY_OPTION ->
                {
                    SelectNotifyKindDialog(parentView).show(
                        requireActivity().supportFragmentManager,
                        "SelectNotifyKindDialog"
                    )
                }
                NavigatorConst.TO_UPDATE_APP_PASSWORD ->
                {
                    activity?.launchActivitiy<LockScreenView> {
                        putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.SET)
                    }
                }
            }
        })

        vm.showSetPassword.observe(this, Observer {
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

    companion object{
        fun newInstance(): SettingFragment
        {
            return SettingFragment()
        }
    }
}