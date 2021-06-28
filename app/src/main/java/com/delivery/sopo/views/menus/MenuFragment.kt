package com.delivery.sopo.views.menus

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentMenuBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.menus.MenuMainFrame
import com.delivery.sopo.viewmodels.menus.MenuViewModel
import com.delivery.sopo.views.main.MainView
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.system.exitProcess

class MenuFragment: Fragment()
{
    private lateinit var parentView: MainView
    private lateinit var menuView: FragmentActivity
    private lateinit var binding: FragmentMenuBinding

    private val vm: MenuViewModel by viewModel()

    lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context)
    {
        super.onAttach(context)

        var pressedTime: Long = 0

        callback = object: OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                FragmentManager.remove(parentView)
                if (System.currentTimeMillis() - pressedTime > 2000)
                {
                    pressedTime = System.currentTimeMillis()
                    Snackbar.make(parentView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다.", 2000).let { bar ->
                        bar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
                    }
                }
                else
                {
                    ActivityCompat.finishAffinity(activity!!)
                    exitProcess(0)
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu, container, false)
        menuView = this.requireActivity()
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
        var pressedTime: Long = 0

        parentView.currentPage.observe(this, Observer {
            if(it != null && it == 2)
            {
                callback = object: OnBackPressedCallback(true)
                {
                    override fun handleOnBackPressed()
                    {
                        SopoLog.d(msg = "MenuFragment:: BackPressListener")

                        if(System.currentTimeMillis() - pressedTime > 2000)
                        {
                            pressedTime = System.currentTimeMillis()
                            val snackbar =
                                Snackbar.make(parentView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다.",
                                              2000)
                            snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
                        }
                        else
                        {
                            ActivityCompat.finishAffinity(activity!!)
                            exitProcess(0)
                        }
                    }
                }

                requireActivity().onBackPressedDispatcher.addCallback(this, callback)
            }
        })

        vm.menu.observe(this, Observer { code ->
            if(code == null) return@Observer
            SopoLog.d("move to code[${code}]")
            when(code)
            {
                TabCode.MENU_NOTICE ->
                {
                    TabCode.MY_MENU_SUB.FRAGMENT = MenuSubFragment.newInstance(TabCode.MENU_NOTICE)
                }
                TabCode.MENU_SETTING ->
                {
                    TabCode.MY_MENU_SUB.FRAGMENT = MenuSubFragment.newInstance(TabCode.MENU_SETTING)
                }
                TabCode.MENU_FAQ ->
                {
                    TabCode.MY_MENU_SUB.FRAGMENT = MenuSubFragment.newInstance(TabCode.MENU_FAQ)
                }
                TabCode.MENU_USE_TERMS ->
                {

                }
                TabCode.MENU_NOT_DISTURB ->
                {
                    TabCode.MY_MENU_SUB.FRAGMENT =
                        MenuSubFragment.newInstance(TabCode.MENU_NOT_DISTURB)
                }
                TabCode.MENU_APP_INFO ->
                {
                    TabCode.MY_MENU_SUB.FRAGMENT =
                        MenuSubFragment.newInstance(TabCode.MENU_APP_INFO)
                }
                TabCode.MENU_ACCOUNT_MANAGEMENT ->
                {
                    TabCode.MY_MENU_SUB.FRAGMENT =
                        MenuSubFragment.newInstance(TabCode.MENU_ACCOUNT_MANAGEMENT)
                }
                else -> throw Exception("Menu is null")
            }
            vm.menu.call()
            FragmentManager.move(parentView, TabCode.MY_MENU_SUB, MenuMainFrame.viewId)
        })
    }

    override fun onResume()
    {
        super.onResume()

        SopoLog.d("onResume()")
    }

    override fun onDetach()
    {
        super.onDetach()
        callback.remove()
    }

    companion object{
        fun newInstance():MenuFragment{
            return MenuFragment()
        }
    }

}