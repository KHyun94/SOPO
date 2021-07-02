package com.delivery.sopo.views.menus

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

                if(fm.fragments[fm.fragments.size - 1] == TabCode.MENU_NOT_DISTURB.FRAGMENT){
                    FragmentManager.remove(parentView)
                    return FragmentManager.move(parentView, TabCode.MENU_SETTING.apply { FRAGMENT = SettingFragment.newInstance() }, viewId)
                }

                FragmentManager.remove(parentView)
                FragmentManager.move(parentView, TabCode.MY_MENU_MAIN.apply { FRAGMENT = MenuFragment.newInstance() }, MenuMainFrame.viewId)
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
        binding = bindView<FragmentMenuSubBinding>(inflater, R.layout.fragment_menu_sub, container)
        viewId = binding.layoutSubMenuFrame.id
        setObserve()
        return binding.root
    }

    private fun<T:ViewDataBinding> bindView(inflater: LayoutInflater, @LayoutRes layoutRes:Int,container: ViewGroup?):T{
        val binding = DataBindingUtil.inflate<T>(inflater, layoutRes, container,false)
        binding.setVariable(BR.vm, vm)
        binding.lifecycleOwner = this
        return binding
    }

    private fun setObserve(){
        vm.tabCode.observe(this, Observer {code ->
            FragmentManager.move(parentView, code, viewId)
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

    override fun onDetach()
    {
        super.onDetach()
        callback.remove()
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