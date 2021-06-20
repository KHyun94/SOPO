package com.delivery.sopo.views.menus

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.delivery.sopo.R
import com.delivery.sopo.enums.MenuEnum
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.databinding.FragmentMenuMainBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.util.AlertUtil
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.factory.MenuViewModelFactory
import com.delivery.sopo.viewmodels.menus.MenuMainFrame
import com.delivery.sopo.viewmodels.menus.MenuViewModel
import com.delivery.sopo.views.main.MainView
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import java.util.function.Function
import kotlin.system.exitProcess


class MenuMainFragment : Fragment()
{
    private val userLocalRepository: UserLocalRepository by inject()
    private val menuVm: MenuViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            MenuViewModelFactory(userLocalRepository)
        ).get(MenuViewModel::class.java)
    }

    private lateinit var menuView: FragmentActivity
    private lateinit var binding: FragmentMenuMainBinding
    private lateinit var parentView: MainView

    var callback: OnBackPressedCallback? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = FragmentMenuMainBinding.inflate(inflater, container, false)
        menuView = this.requireActivity()
        parentView = activity as MainView

        viewBinding()
        setObserver()

        return binding.root
    }


    private fun viewBinding()
    {
        binding.vm = menuVm
        binding.lifecycleOwner = this
    }

    fun setObserver()
    {
        var pressedTime: Long = 0

        parentView.currentPage.observe(this, Observer {
            if (it != null && it == 2)
            {
                callback = object : OnBackPressedCallback(true)
                {
                    override fun handleOnBackPressed()
                    {
                        SopoLog.d( msg = "MenuFragment:: BackPressListener")

                        if (System.currentTimeMillis() - pressedTime > 2000)
                        {
                            pressedTime = System.currentTimeMillis()
                            val snackbar = Snackbar.make(
                                parentView.binding.layoutMain,
                                "한번 더 누르시면 앱이 종료됩니다.",
                                2000
                            )
                            snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
                        }
                        else
                        {
                            ActivityCompat.finishAffinity(activity!!)
                            exitProcess(0)
                        }
                    }
                }

                requireActivity().onBackPressedDispatcher.addCallback(this, callback!!)
            }
        })

        menuVm.menu.observe(this, Observer {code ->
            when (code)
            {
                TabCode.MENU_NOTICE -> {
                    TabCode.MY_MENU_SUB.FRAGMENT = MenuSubFragment.newInstance(TabCode.MENU_NOTICE)
                }
                TabCode.MENU_SETTING ->
                {
                    TabCode.MY_MENU_SUB.FRAGMENT = MenuSubFragment.newInstance(TabCode.MENU_SETTING)
                }
            }

            FragmentManager.move(parentView, TabCode.MY_MENU_SUB, MenuMainFrame.viewId)
        })

//        menuVm.menu.observe(this, Observer { enum ->
//            when (enum)
//            {
//                MenuEnum.NOTICE ->
//                {
//                    move(menuView, NoticeFragment(), 0)
//                }
//                MenuEnum.SETTING ->
//                {
//                    TabCode.MY_MENU_SUB.FRAGMENT = MenuSubFragment.newInstance(TabCode.MENU_SETTING)
//                    FragmentManager.move(parentView, TabCode.MY_MENU_SUB, viewId)
////                    move(menuView, SettingFragment(), 0)
//                }
//                MenuEnum.FAQ ->
//                {
//                    move(menuView, FaqFragment(), 0)
//                }
//                MenuEnum.USE_TERMS ->
//                {
//                    move(menuView, AppInfoFragment(), 0)
//                }
//                MenuEnum.APP_INFO ->
//                {
//                    move(menuView, AppInfoFragment(), 0)
//                }
//                MenuEnum.NOT_DISTURB ->
//                {
//                    move(menuView, NotDisturbTimeFragment(), 0)
//                }
//                MenuEnum.ACCOUNT_MANAGER ->
//                {
//                    move(menuView, AccountManagerFragment(), 0)
//                }
//                MenuEnum.SIGN_OUT ->
//                {
//                    move(menuView, SignOutView(), 0)
//                }
//                MenuEnum.UPDATE_NICKNAME ->
//                {
////                    move(menuView, UpdateNicknameView(), 0)
//                }
//            }
//        })


    }

    override fun onDetach()
    {
        super.onDetach()
        if (callback != null) callback!!.remove()
    }

}