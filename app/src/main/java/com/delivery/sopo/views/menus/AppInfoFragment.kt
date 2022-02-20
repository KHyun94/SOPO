package com.delivery.sopo.views.menus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.delivery.sopo.R
import com.delivery.sopo.databinding.AppInfoViewBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.menus.AppInfoViewModel
import com.delivery.sopo.viewmodels.menus.MenuMainFragment
import com.delivery.sopo.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class AppInfoFragment: BaseFragment<AppInfoViewBinding, AppInfoViewModel>()
{
    override val layoutRes: Int = R.layout.app_info_view
    override val vm: AppInfoViewModel by viewModel()
    override val mainLayout: View by lazy { binding.constraintMainAppInfo }

    private val parentView: MainView by lazy { activity as MainView }

    override fun setBeforeBinding()
    {
        super.setBeforeBinding()

        SopoLog.d("FAQFAQ")

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
        parentView.currentPage.observe(this) {
            if(it != 2) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }
    }

    companion object
    {
        fun newInstance() = AppInfoFragment()
    }

}