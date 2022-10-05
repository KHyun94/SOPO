package com.delivery.sopo.presentation.views.menus

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import com.delivery.sopo.BR
import com.delivery.sopo.R
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.databinding.FragmentMenuSubBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.presentation.viewmodels.menus.MenuMainFragment
import com.delivery.sopo.presentation.viewmodels.menus.MenuSubViewModel
import com.delivery.sopo.presentation.views.main.MainActivity
import org.koin.android.ext.android.inject

class MenuSubFragment: Fragment()
{
    lateinit var parentActivity: MainActivity
    lateinit var binding: FragmentMenuSubBinding
    private val vm: MenuSubViewModel by inject()

    var callback: OnBackPressedCallback

    init
    {
        callback = object: OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                moveToBack()
            }
        }
    }

    override fun onAttach(context: Context)
    {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        parentActivity = activity as MainActivity

        receiveBundleData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = bindView<FragmentMenuSubBinding>(inflater, R.layout.fragment_menu_sub, container)
        viewId = binding.layoutSubMenuFrame.id
        setObserve()
        return binding.root
    }

    private fun <T: ViewDataBinding> bindView(inflater: LayoutInflater, @LayoutRes layoutRes: Int, container: ViewGroup?): T
    {
        val binding = DataBindingUtil.inflate<T>(inflater, layoutRes, container, false)
        binding.setVariable(BR.vm, vm)
        binding.lifecycleOwner = this
        return binding
    }

    override fun onResume()
    {
        super.onResume()
        callback = object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                moveToBack()
            }
        }
        parentActivity.onBackPressedDispatcher.addCallback(parentActivity, callback)
    }

    private fun setObserve()
    {
//        parentView.getCurrentPage().observe(this, Observer {
//            if (it != null && it == TabCode.thirdTab)
//            {
//                callback = object : OnBackPressedCallback(true)
//                {
//                    override fun handleOnBackPressed()
//                    {
//                        moveToBack()
//                    }
//                }
//                requireActivity().onBackPressedDispatcher.addCallback(this, callback)
//            }
//        })


        vm.navigator.observe(this, Observer { navigator ->
            SopoLog.d("navigator[$navigator]")

            if(navigator == NavigatorConst.Event.BACK)
            {
                moveToBack()
                return@Observer
            }

            val enumData = CodeUtil.getEnumValueOfName<TabCode>(navigator)

            enumData.FRAGMENT = when(enumData)
            {
                TabCode.MENU_NOTICE -> NoticeFragment.newInstance()
                TabCode.MENU_SETTING -> SettingFragment.newInstance()
                TabCode.MENU_FAQ -> FaqFragment.newInstance()
                TabCode.MENU_APP_INFO -> AppInfoFragment.newInstance()
                TabCode.MENU_ACCOUNT_MANAGEMENT -> AccountManagerFragment.newInstance()
                else -> throw Exception("Menu is null")
            }

            vm.title.postValue(enumData.TITLE)
            FragmentManager.move(parentActivity, enumData, binding.layoutSubMenuFrame.id)
        })

    }

    private fun receiveBundleData()
    {
        arguments?.let { bundle ->
            val name = bundle.getString("MENU_SUB")
            vm.navigator.value = name
        }
    }

    override fun onDetach()
    {
        super.onDetach()
        callback.remove()
    }

    fun moveToBack(){
        FragmentManager.run {
            remove(parentActivity)
            move(parentActivity, TabCode.MY_MENU_MAIN.apply { FRAGMENT = MenuFragment.newInstance() }, MenuMainFragment.viewId)
        }
    }

    companion object
    {
        @IdRes var viewId: Int = 0

        fun newInstance(name: String): MenuSubFragment
        {
            return MenuSubFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("MENU_SUB", name)
                }
            }
        }
    }

}