package com.delivery.sopo.views.menus

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentMenuSubBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.menus.MenuMainFrame
import com.delivery.sopo.viewmodels.menus.MenuSubViewModel
import com.delivery.sopo.views.main.MainView
import org.koin.android.ext.android.inject

class MenuSubFragment: Fragment()
{
    lateinit var parentView: MainView
    lateinit var binding: FragmentMenuSubBinding
    private val vm: MenuSubViewModel by inject()

    lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context)
    {
        super.onAttach(context)

        callback = object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fm = parentView.supportFragmentManager
                val childFm = childFragmentManager
                SopoLog.d("""
                    Fragment 데이터
                    ${fm.backStackEntryCount} && ${fm.fragments.size}
                    ${childFm.backStackEntryCount} && ${childFm.fragments.size}
                """.trimIndent())
                FragmentManager.remove(parentView)
                FragmentManager.move(parentView, TabCode.MY_MENU_MAIN, MenuMainFrame.viewId)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        parentView = activity as MainView

        receiveBundleData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu_sub, container, false)
        binding.vm = vm
        binding.lifecycleOwner = this
        viewId = binding.layoutSubMenuFrame.id
        setObserve()
        return binding.root
    }

    private fun setObserve(){
        vm.tabCode.observe(this, Observer {code ->
            code ?: return@Observer
            SopoLog.d("tabCode:$code")
            FragmentManager.add(parentView, code, binding.layoutSubMenuFrame.id)
        })
    }

    private fun receiveBundleData()
    {
        arguments?.let { bundle ->
            val tabCode = bundle.getSerializable("MENU_SUB") as TabCode
            vm.tabCode.value = tabCode
            SopoLog.d("receiveBundleData >>> $tabCode ${vm.tabCode.value}")
        }
    }

    override fun onResume()
    {
        super.onResume()
        SopoLog.d("onResume()")
    }

    companion object{

        @IdRes
        var viewId: Int = 0

        fun newInstance(code: TabCode): MenuSubFragment{

            val args = Bundle().apply {
                putSerializable("MENU_SUB", code)
            }

            return MenuSubFragment().apply {
                arguments = args
            }
        }
    }

}