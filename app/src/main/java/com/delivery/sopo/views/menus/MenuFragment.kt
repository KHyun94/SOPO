package com.delivery.sopo.views.menus

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.BR
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentMenuBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.menus.MenuMainFragment
import com.delivery.sopo.viewmodels.menus.MenuViewModel
import com.delivery.sopo.views.main.MainView
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.KoinComponent
import kotlin.system.exitProcess

class MenuFragment: Fragment(), KoinComponent
{
    private lateinit var parentView: MainView
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
                if(System.currentTimeMillis() - pressedTime > 2000)
                {
                    pressedTime = System.currentTimeMillis()
                    Snackbar.make(parentView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다.", 2000)
                        .let { bar ->
                            bar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
                        }
                }
                else
                {
                    ActivityCompat.finishAffinity(requireActivity())
                    exitProcess(0)
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = bindView(inflater, R.layout.fragment_menu, container)
        parentView = activity as MainView

        setObserver()

        return binding.root
    }

    private fun <T: ViewDataBinding> bindView(inflater: LayoutInflater,
                                              @LayoutRes layoutRes: Int, container: ViewGroup?): T
    {
        val binding = DataBindingUtil.inflate<T>(inflater, layoutRes, container, false)
        binding.setVariable(BR.vm, vm)
        binding.lifecycleOwner = this
        return binding
    }

    override fun onResume()
    {
        super.onResume()

        var pressedTime: Long = 0

        callback = object: OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                if(System.currentTimeMillis() - pressedTime > 2000)
                {
                    pressedTime = System.currentTimeMillis()

                    Snackbar.make(parentView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다.", 2000)
                        .apply {
                            animationMode = Snackbar.ANIMATION_MODE_SLIDE
                        }
                        .show()

                    return
                }

                ActivityCompat.finishAffinity(requireActivity())
                exitProcess(0)
            }
        }
        parentView.onBackPressedDispatcher.addCallback(parentView, callback)
    }

    fun setObserver()
    {
        //        var pressedTime: Long = 0

        //        parentView.currentPage.observe(this, Observer { page ->
        //            if(page != null && page == TabCode.thirdTab)
        //            {
        //                callback = object: OnBackPressedCallback(true)
        //                {
        //                    override fun handleOnBackPressed()
        //                    {
        //                        if(System.currentTimeMillis() - pressedTime > 2000)
        //                        {
        //                            pressedTime = System.currentTimeMillis()
        //
        //                            Snackbar.make(parentView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다.", 2000).apply {
        //                                animationMode = Snackbar.ANIMATION_MODE_SLIDE
        //                            }.show()
        //
        //                            return
        //                        }
        //
        //                        ActivityCompat.finishAffinity(requireActivity())
        //                        exitProcess(0)
        //                    }
        //                }
        //
        //                requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        //            }
        //        })

        vm.menu.observe(this, Observer { code ->
            SopoLog.d("move to code[${code}]")
            when(code)
            {
                TabCode.MENU_NOTICE ->
                {
                    TabCode.MY_MENU_SUB.FRAGMENT =
                        MenuSubFragment.newInstance(TabCode.MENU_NOTICE.NAME)
                }
                TabCode.MENU_SETTING ->
                {
                    TabCode.MY_MENU_SUB.FRAGMENT =
                        MenuSubFragment.newInstance(TabCode.MENU_SETTING.NAME)
                }
                TabCode.MENU_FAQ ->
                {
                    TabCode.MY_MENU_SUB.FRAGMENT =
                        MenuSubFragment.newInstance(TabCode.MENU_FAQ.NAME)
                }
                TabCode.MENU_USE_TERMS ->
                {

                }
                TabCode.MENU_APP_INFO ->
                {
                    TabCode.MY_MENU_SUB.FRAGMENT =
                        MenuSubFragment.newInstance(TabCode.MENU_APP_INFO.NAME)
                }
                TabCode.MENU_ACCOUNT_MANAGEMENT ->
                {
                    TabCode.MY_MENU_SUB.FRAGMENT =
                        MenuSubFragment.newInstance(TabCode.MENU_ACCOUNT_MANAGEMENT.NAME)
                }
                else -> throw Exception("Menu is null")
            }

            FragmentManager.move(parentView, TabCode.MY_MENU_SUB, MenuMainFragment.viewId)
        })
    }

    override fun onDetach()
    {
        super.onDetach()
        callback.remove()
    }

    companion object
    {
        fun newInstance(): MenuFragment
        {
            return MenuFragment()
        }
    }
}