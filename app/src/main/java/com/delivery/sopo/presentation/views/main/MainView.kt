package com.delivery.sopo.presentation.views.main

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.delivery.sopo.OnCallbackListener
import com.delivery.sopo.R
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.databinding.ItemMainTabBinding
import com.delivery.sopo.databinding.MainViewBinding
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.enums.SnackBarEnum
import com.delivery.sopo.enums.SnackBarType
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.makeGone
import com.delivery.sopo.extensions.makeVisible
import com.delivery.sopo.interfaces.OnPageSelectListener
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.models.base.OnActivityResultCallbackListener
import com.delivery.sopo.presentation.consts.IntentConst
import com.delivery.sopo.presentation.models.TabIcon
import com.delivery.sopo.presentation.services.PowerManager
import com.delivery.sopo.presentation.viewmodels.main.MainViewModel
import com.delivery.sopo.presentation.viewmodels.menus.MenuMainFragment
import com.delivery.sopo.presentation.views.adapter.ViewPagerAdapter
import com.delivery.sopo.presentation.views.inquiry.InquiryMainFragment
import com.delivery.sopo.presentation.views.menus.LockScreenView
import com.delivery.sopo.presentation.views.registers.RegisterMainFragment
import com.delivery.sopo.util.ClipboardUtil
import com.delivery.sopo.util.ui_util.BottomNotificationBar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainView: BaseView<MainViewBinding, MainViewModel>(), OnPageSelectListener
{
    override val layoutRes: Int = R.layout.main_view
    override val vm: MainViewModel by viewModel()
    override val mainLayout: View by lazy { binding.viewPagerMain }

    var registerTabBinding: ItemMainTabBinding? = null
    var inquiryTabBinding: ItemMainTabBinding? = null
    var menuTabBinding: ItemMainTabBinding? = null

    private var registerTabIcon =
        TabIcon(R.drawable.ic_activate_register, R.drawable.ic_inactivate_register)
    private var inquiryTabIcon =
        TabIcon(R.drawable.ic_activate_inquiry, R.drawable.ic_inactivate_inquiry)
    private var menuTabIcon = TabIcon(R.drawable.ic_activate_menu, R.drawable.ic_inactivate_menu)

    private val baseFragments =
        arrayListOf(RegisterMainFragment(), InquiryMainFragment(), MenuMainFragment())
    lateinit var onReselectedTapClickListener: OnCallbackListener

    override fun onBeforeBinding()
    {
        super.onBeforeBinding()
        PowerManager.checkWhiteList(this)
    }

    override fun onAfterBinding()
    {
        super.onAfterBinding()

        snackBar = binding.bottomNotificationBar

        checkAppPassword()
        setViewPager()
        setTabLayout()
        checkInitializedTab()
    }

    override fun onDestroy()
    {
        super.onDestroy()

        registerTabBinding?.unbind()
        inquiryTabBinding?.unbind()
        menuTabBinding?.unbind()

        registerTabBinding = null
        inquiryTabBinding = null
        menuTabBinding = null
    }

    fun getCurrentPage(): LiveData<Int> = vm.currentPage

    private fun checkInitializedTab() = CoroutineScope(Dispatchers.Default).launch {

        val clipBoardData = ClipboardUtil.pasteClipboardText(context = this@MainView)

        clipBoardData?.let {
            binding.viewPagerMain.currentItem = 0
            return@launch
        }

        val isExistParcel = vm.checkIsExistParcels()

        if(isExistParcel)
        {
            binding.viewPagerMain.currentItem = 1
        }
        else
        {
            binding.viewPagerMain.currentItem = 0
        }
    }

    fun checkAppPassword() = vm.hasAppPassword { hasAppPassword ->

        if(!hasAppPassword) return@hasAppPassword

        val onActivityResultCallbackListener = object: OnActivityResultCallbackListener
        {
            override fun callback(activityResult: ActivityResult)
            {
                if(activityResult.resultCode != Activity.RESULT_CANCELED) return
                exit()
            }
        }

        setOnActivityResultCallbackListener(onActivityResultCallbackListener)

        val intent = Intent(this@MainView, LockScreenView::class.java).apply {
            putExtra(IntentConst.Extra.LOCK_STATUS_TYPE, LockScreenStatusEnum.VERIFY)
        }

        launchActivityResult(intent)
    }

    fun activateTab(binding: ItemMainTabBinding, tabIcon: TabIcon)
    {
        binding.ivTab.setBackgroundResource(tabIcon.activate)
        binding.tvTabName.setTextColor(ContextCompat.getColor(this@MainView, R.color.COLOR_MAIN_700))
    }

    fun inactivateTab(binding: ItemMainTabBinding, tabIcon: TabIcon)
    {
        binding.ivTab.setBackgroundResource(tabIcon.inactivate)
        binding.tvTabName.setTextColor(ContextCompat.getColor(this@MainView, R.color.COLOR_GRAY_400))
    }

    private fun setViewPager()
    {
        binding.viewPagerMain.isUserInputEnabled = false

        val adapter = ViewPagerAdapter(this, baseFragments)

        binding.viewPagerMain.adapter = adapter
        binding.viewPagerMain.offscreenPageLimit = 2

        val onTabSelectedListener = object: TabLayout.OnTabSelectedListener
        {
            override fun onTabSelected(tab: TabLayout.Tab?)
            {
                vm.postPage(tab?.position ?: 0)

                when(tab?.position)
                {
                    NavigatorConst.REGISTER_TAB -> registerTabBinding?.let { activateTab(it, registerTabIcon) }
                    NavigatorConst.INQUIRY_TAB -> inquiryTabBinding?.let { activateTab(it, inquiryTabIcon) }
                    NavigatorConst.MY_MENU_TAB -> menuTabBinding?.let { activateTab(it, menuTabIcon) }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?)
            {
                when(tab?.position)
                {
                    NavigatorConst.REGISTER_TAB -> registerTabBinding?.let { inactivateTab(it, registerTabIcon) }
                    NavigatorConst.INQUIRY_TAB -> inquiryTabBinding?.let { inactivateTab(it, inquiryTabIcon) }
                    NavigatorConst.MY_MENU_TAB -> menuTabBinding?.let { inactivateTab(it, menuTabIcon) }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?)
            {
                when(tab?.position ?: return)
                {
                    NavigatorConst.INQUIRY_TAB -> onReselectedTapClickListener()
                    else -> return
                }
            }
        }

        binding.layoutMainTab.addOnTabSelectedListener(onTabSelectedListener)
    }

    private fun setTabLayout()
    {
        TabLayoutMediator(binding.layoutMainTab, binding.viewPagerMain) { tab, pos ->
            setTab(tab, pos)
        }.attach()
    }

    private fun setTab(tab: TabLayout.Tab, pos: Int)
    {
        tab.setCustomView(R.layout.item_main_tab)

        when(pos)
        {
            TabCode.firstTab -> registerTabBinding =
                setTabIcon(tab, R.drawable.ic_activate_register, "등록", R.color.COLOR_MAIN_700)
            TabCode.secondTab -> inquiryTabBinding =
                setTabIcon(tab, R.drawable.ic_inactivate_inquiry, "조회", R.color.COLOR_GRAY_400)
            TabCode.thirdTab -> menuTabBinding =
                setTabIcon(tab, R.drawable.ic_inactivate_menu, "메뉴", R.color.COLOR_GRAY_400)
        }
    }

    private fun setTabIcon(tab: TabLayout.Tab, @DrawableRes
    iconRes: Int, tabName: String, textColor: Int): ItemMainTabBinding
    {
        val tabBinding =
            ItemMainTabBinding.bind(tab.customView ?: throw NullPointerException("TAB is null"))

        tabBinding.ivTab.setBackgroundResource(iconRes)
        tabBinding.tvTabName.text = tabName
        tabBinding.tvTabName.setTextColor(ContextCompat.getColor(this@MainView, textColor))

        return tabBinding
    }

    fun showTab()
    {
        binding.layoutMainTab.makeVisible()

        binding.viewPagerMain.isUserInputEnabled = false
        binding.layoutMainTab.getTabAt(0)?.view?.makeVisible()
        binding.layoutMainTab.getTabAt(1)?.view?.makeVisible()
        binding.layoutMainTab.getTabAt(2)?.view?.makeVisible()
    }

    fun hideTab()
    {
        binding.layoutMainTab.makeGone()

        binding.viewPagerMain.isUserInputEnabled = false

        binding.layoutMainTab.getTabAt(0)?.view?.makeGone()
        binding.layoutMainTab.getTabAt(1)?.view?.makeGone()
        binding.layoutMainTab.getTabAt(2)?.view?.makeGone()
    }

    override fun onChangeTab(tab: TabCode?)
    {
        inquiryTabIcon = if(tab == TabCode.INQUIRY_ONGOING || tab == TabCode.INQUIRY_COMPLETE)
        {
            TabIcon(R.drawable.ic_bttn_arrow_up, R.drawable.ic_bttn_arrow_up_inactive)
        }
        else
        {
            TabIcon(R.drawable.ic_activate_inquiry, R.drawable.ic_inactivate_inquiry)
        }

        if(vm.currentPage.value == TabCode.secondTab)
        {
            inquiryTabBinding?.ivTab?.background =
                ContextCompat.getDrawable(this, inquiryTabIcon.activate)
        }
        else
        {
            inquiryTabBinding?.ivTab?.background =
                ContextCompat.getDrawable(this, inquiryTabIcon.inactivate)
        }
    }

    override fun onSetCurrentPage(page: Int)
    {
        binding.viewPagerMain.setCurrentItem(page, true)
    }

    override fun onActivateNetwork()
    {
        super.onActivateNetwork()

        dismiss()
        val connectNetwork = SnackBarType.ConnectNetwork("네트워크가 다시 연결되었어요.", 3000)
        show(connectNetwork)
        vm.stopToCheckNetworkStatus()
    }

    override fun onDeactivateNetwork()
    {
        super.onDeactivateNetwork()

        val disconnectNetwork = SnackBarType.DisconnectNetwork("네트워크 오류입니다.", 0)
        show(disconnectNetwork)
    }
}