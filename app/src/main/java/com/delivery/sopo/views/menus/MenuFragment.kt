package com.delivery.sopo.views.menus

import android.view.View
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentMenuBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.menus.MenuMainFragment
import com.delivery.sopo.viewmodels.menus.MenuViewModel
import com.delivery.sopo.views.main.MainView
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class MenuFragment: BaseFragment<FragmentMenuBinding, MenuViewModel>()
{
    override val vm: MenuViewModel by viewModel()
    override val layoutRes: Int = R.layout.fragment_menu
    override val mainLayout: View by lazy { binding.constraintMainMenu }

    private  val parentView: MainView by lazy { (requireActivity() as MainView) }

    override fun setBeforeBinding()
    {
        super.setBeforeBinding()

        onSOPOBackPressedListener = object: OnSOPOBackPressEvent()
        {
            override fun onBackPressedInTime()
            {
                Snackbar.make(parentView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다.", 2000).apply { animationMode = Snackbar.ANIMATION_MODE_SLIDE }.show()
            }

            override fun onBackPressedOutTime()
            {
                exit()
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

    companion object
    {
        fun newInstance(): MenuFragment
        {
            return MenuFragment()
        }
    }


}