package com.delivery.sopo.presentation.views.menus

import android.view.View
import androidx.fragment.app.viewModels
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentMenuBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.presentation.viewmodels.menus.MenuMainFragment
import com.delivery.sopo.presentation.viewmodels.menus.MenuViewModel
import com.delivery.sopo.presentation.views.main.MainActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.koin.androidx.viewmodel.ext.android.viewModel

@AndroidEntryPoint
class MenuFragment: BaseFragment<FragmentMenuBinding, MenuViewModel>()
{
    override val vm: MenuViewModel by viewModels()
    override val layoutRes: Int = R.layout.fragment_menu
    override val mainLayout: View by lazy { binding.constraintMainMenu }

    private  val parentActivity: MainActivity by lazy { (requireActivity() as MainActivity) }

    override fun setBeforeBinding()
    {
        super.setBeforeBinding()

        onSOPOBackPressedListener = object: OnSOPOBackPressEvent()
        {
            override fun onBackPressedInTime()
            {
                Snackbar.make(mainLayout, "한번 더 누르시면 앱이 종료됩니다.", 2000).apply { animationMode = Snackbar.ANIMATION_MODE_SLIDE }.show()
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
        parentActivity.getCurrentPage().observe(this) {
            if(it != 2) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.menu.observe(this) { code ->
            SopoLog.d("move to code[${code}]")
            when(code)
            {
                TabCode.MENU_NOTICE ->
                {
                    TabCode.MY_MENU_SUB.FRAGMENT = MenuSubFragment.newInstance(TabCode.MENU_NOTICE.NAME)
                }
                TabCode.MENU_SETTING ->
                {
                    TabCode.MY_MENU_SUB.FRAGMENT = MenuSubFragment.newInstance(TabCode.MENU_SETTING.NAME)
                }
                TabCode.MENU_FAQ ->
                {
                    TabCode.MY_MENU_SUB.FRAGMENT = MenuSubFragment.newInstance(TabCode.MENU_FAQ.NAME)
                }
                TabCode.MENU_USE_TERMS ->
                {

                }
                TabCode.MENU_APP_INFO ->
                {
                    TabCode.MY_MENU_SUB.FRAGMENT = MenuSubFragment.newInstance(TabCode.MENU_APP_INFO.NAME)
                }
                TabCode.MENU_ACCOUNT_MANAGEMENT ->
                {
                    TabCode.MY_MENU_SUB.FRAGMENT = MenuSubFragment.newInstance(TabCode.MENU_ACCOUNT_MANAGEMENT.NAME)
                }
                else -> throw Exception("Menu is null")
            }

            FragmentManager.move(parentActivity, TabCode.MY_MENU_SUB, MenuMainFragment.viewId)
        }
    }

    override fun onResume()
    {
        super.onResume()

        SopoLog.d("MenuFragment onResume(...)")
    }

    companion object
    {
        fun newInstance(): MenuFragment
        {
            return MenuFragment()
        }
    }


}