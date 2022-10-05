package com.delivery.sopo.presentation.views.menus

import android.view.View
import androidx.fragment.app.viewModels
import com.delivery.sopo.R
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.databinding.AppInfoViewBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.presentation.viewmodels.menus.AppInfoViewModel
import com.delivery.sopo.presentation.viewmodels.menus.MenuMainFragment
import com.delivery.sopo.presentation.views.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import org.koin.androidx.viewmodel.ext.android.viewModel

@AndroidEntryPoint
class AppInfoFragment: BaseFragment<AppInfoViewBinding, AppInfoViewModel>()
{
    override val layoutRes: Int = R.layout.app_info_view
    override val vm: AppInfoViewModel by viewModels()
    override val mainLayout: View by lazy { binding.constraintMainAppInfo }

    private val parentActivity: MainActivity by lazy { activity as MainActivity }

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
        parentActivity.getCurrentPage().observe(this) {
            if(it != 2) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.navigator.observe(this) { nav ->
            when(nav)
            {
                NavigatorConst.Event.BACK ->
                {
                    FragmentManager.refreshMove(parentActivity, TabCode.MY_MENU_MAIN.apply {
                        FRAGMENT = MenuFragment.newInstance()
                    }, MenuMainFragment.viewId)
                }
            }
        }
    }

    companion object
    {
        fun newInstance() = AppInfoFragment()
    }

}