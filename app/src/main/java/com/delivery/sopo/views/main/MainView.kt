package com.delivery.sopo.views.main

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.view.KeyEvent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.delivery.sopo.OnCallbackListener
import com.delivery.sopo.R
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.ItemMainTabBinding
import com.delivery.sopo.databinding.MainViewBinding
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.makeGone
import com.delivery.sopo.extensions.makeVisible
import com.delivery.sopo.interfaces.OnPageSelectListener
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.models.base.OnActivityResultCallbackListener
import com.delivery.sopo.services.PowerManager
import com.delivery.sopo.services.receivers.RefreshParcelBroadcastReceiver
import com.delivery.sopo.util.ClipboardUtil
import com.delivery.sopo.viewmodels.main.MainViewModel
import com.delivery.sopo.viewmodels.menus.MenuMainFragment
import com.delivery.sopo.views.adapter.ViewPagerAdapter
import com.delivery.sopo.views.inquiry.InquiryMainFragment
import com.delivery.sopo.views.menus.LockScreenView
import com.delivery.sopo.views.registers.RegisterMainFragment
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
    override val mainLayout: View by lazy { binding.layoutViewPager }

    var tab1stBinding: ItemMainTabBinding? = null
    var tab2ndBinding: ItemMainTabBinding? = null
    var tab3rdBinding: ItemMainTabBinding? = null

    private var inquiryTabIcon: Pair<Int, Int> =
        Pair(R.drawable.ic_activate_inquiry, R.drawable.ic_inactivate_inquiry)

    private val baseFragments =
        arrayListOf(RegisterMainFragment(), InquiryMainFragment(), MenuMainFragment())

    lateinit var onTabReselectListener: OnCallbackListener

    private val refreshParcelBroadcastReceiver = RefreshParcelBroadcastReceiver()

    override fun onBeforeBinding()
    {
        super.onBeforeBinding()

        PowerManager.checkWhiteList(this)
    }

    override fun onAfterBinding()
    {
        super.onAfterBinding()

        checkAppPassword()
        setViewPager()
        setTabLayout()
        checkInitializedTab()


    }

    override fun onResume()
    {
        super.onResume()
        registerReceiver(refreshParcelBroadcastReceiver, IntentFilter().apply { addAction(RefreshParcelBroadcastReceiver.COMPLETE_REGISTER_ACTION) })
    }

    override fun onPause()
    {
        super.onPause()
        unregisterReceiver(refreshParcelBroadcastReceiver)
    }

    override fun onDestroy()
    {
        super.onDestroy()

        tab1stBinding?.unbind()
        tab2ndBinding?.unbind()
        tab3rdBinding?.unbind()

        tab1stBinding = null
        tab2ndBinding = null
        tab3rdBinding = null
    }

    fun getCurrentPage(): LiveData<Int>
    {
        return vm.currentPage
    }

    private fun checkInitializedTab() = CoroutineScope(Dispatchers.Default).launch {
        val clipBoardData = ClipboardUtil.pasteClipboardText(context = this@MainView)

        clipBoardData?.let {
            binding.layoutViewPager.currentItem = 0
            return@launch
        }

        val isExistParcel = vm.checkIsExistParcels()

        if(isExistParcel)
        {
            binding.layoutViewPager.currentItem = 1
        }
        else
        {
            binding.layoutViewPager.currentItem = 0
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
            putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.VERIFY)
        }

        launchActivityResult(intent)
    }

    fun activateTab(binding: ItemMainTabBinding, resId: Int)
    {
        binding.ivTab.setBackgroundResource(resId)
        binding.tvTabName.setTextColor(ContextCompat.getColor(this@MainView, R.color.COLOR_MAIN_700))
    }

    fun inactivateTab(binding: ItemMainTabBinding, resId: Int)
    {
        binding.ivTab.setBackgroundResource(resId)
        binding.tvTabName.setTextColor(ContextCompat.getColor(this@MainView, R.color.COLOR_GRAY_400))
    }

    private fun setViewPager()
    {
        binding.layoutViewPager.isUserInputEnabled = false
        val adapter = ViewPagerAdapter(this, baseFragments)

        binding.layoutViewPager.adapter = adapter
        binding.layoutViewPager.offscreenPageLimit = 2

        val onTabSelectedListener = object: TabLayout.OnTabSelectedListener
        {
            override fun onTabSelected(tab: TabLayout.Tab?)
            {
                vm.postPage(tab?.position ?: 0)

                when(tab?.position)
                {
                    NavigatorConst.REGISTER_TAB -> tab1stBinding?.let { activateTab(it, R.drawable.ic_activate_register) }
                    NavigatorConst.INQUIRY_TAB -> tab2ndBinding?.let { activateTab(it, inquiryTabIcon.first) }
                    NavigatorConst.MY_MENU_TAB -> tab3rdBinding?.let { activateTab(it, R.drawable.ic_activate_menu) }
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?)
            {
                when(tab?.position)
                {
                    NavigatorConst.REGISTER_TAB -> tab1stBinding?.let { inactivateTab(it, R.drawable.ic_inactivate_register) }
                    NavigatorConst.INQUIRY_TAB -> tab2ndBinding?.let { inactivateTab(it, inquiryTabIcon.second) }
                    NavigatorConst.MY_MENU_TAB -> tab3rdBinding?.let { inactivateTab(it, R.drawable.ic_inactivate_menu) }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?)
            {
                when(tab?.position ?: return)
                {
                    NavigatorConst.REGISTER_TAB, NavigatorConst.MY_MENU_TAB ->
                    {
                    }
                    NavigatorConst.INQUIRY_TAB ->
                    {
                        onTabReselectListener.invoke()
                    }
                }
            }
        }

        binding.layoutMainTab.addOnTabSelectedListener(onTabSelectedListener)
    }

    private fun setTabLayout()
    {
        TabLayoutMediator(binding.layoutMainTab, binding.layoutViewPager) { tab, pos ->
            setTab(tab, pos)
        }.attach()
    }


    private fun setTab(tab: TabLayout.Tab, pos: Int)
    {
        tab.setCustomView(R.layout.item_main_tab)

        when(pos)
        {
            TabCode.firstTab -> tab1stBinding =
                setTabIcon(tab, R.drawable.ic_activate_register, "등록", R.color.COLOR_MAIN_700)
            TabCode.secondTab -> tab2ndBinding =
                setTabIcon(tab, R.drawable.ic_inactivate_inquiry, "조회", R.color.COLOR_GRAY_400)
            TabCode.thirdTab -> tab3rdBinding =
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

        binding.layoutViewPager.isUserInputEnabled = false
        binding.layoutMainTab.getTabAt(0)?.view?.makeVisible()
        binding.layoutMainTab.getTabAt(1)?.view?.makeVisible()
        binding.layoutMainTab.getTabAt(2)?.view?.makeVisible()
    }

    fun hideTab()
    {
        binding.layoutMainTab.makeGone()

        binding.layoutViewPager.isUserInputEnabled = false

        binding.layoutMainTab.getTabAt(0)?.view?.makeGone()
        binding.layoutMainTab.getTabAt(1)?.view?.makeGone()
        binding.layoutMainTab.getTabAt(2)?.view?.makeGone()
    }

    override fun onChangeTab(tab: TabCode?)
    {
        inquiryTabIcon = if(tab == TabCode.INQUIRY_ONGOING || tab == TabCode.INQUIRY_COMPLETE)
        {
            Pair(R.drawable.ic_bttn_arrow_up, R.drawable.ic_bttn_arrow_up_inactive)
        }
        else
        {
            Pair(R.drawable.ic_activate_inquiry, R.drawable.ic_inactivate_inquiry)
        }

        if(vm.currentPage.value == TabCode.secondTab)
        {
            tab2ndBinding?.ivTab?.background = ContextCompat.getDrawable(this, inquiryTabIcon.first)
        }
        else
        {
            tab2ndBinding?.ivTab?.background =
                ContextCompat.getDrawable(this, inquiryTabIcon.second)
        }
    }

    override fun onMoveToPage(page: Int)
    {
        binding.layoutViewPager.setCurrentItem(page, true)
    }
}