package com.delivery.sopo.views.main

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.local.app_password.AppPasswordRepository
import com.delivery.sopo.databinding.ItemMainTabBinding
import com.delivery.sopo.databinding.MainViewBinding
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.reduceSensitive
import com.delivery.sopo.interfaces.OnPageSelectListener
import com.delivery.sopo.interfaces.OnTapReselectListener
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.models.base.OnActivityResultCallbackListener
import com.delivery.sopo.services.PowerManager
import com.delivery.sopo.services.receivers.RefreshParcelBroadcastReceiver
import com.delivery.sopo.util.ClipboardUtil
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.main.MainViewModel
import com.delivery.sopo.viewmodels.menus.MenuMainFragment
import com.delivery.sopo.views.adapter.ViewPagerAdapter
import com.delivery.sopo.views.inquiry.InquiryFragment
import com.delivery.sopo.views.inquiry.InquiryMainFragment
import com.delivery.sopo.views.menus.LockScreenView
import com.delivery.sopo.views.menus.MenuFragment
import com.delivery.sopo.views.registers.InputParcelFragment
import com.delivery.sopo.views.registers.RegisterMainFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainView: BaseView<MainViewBinding, MainViewModel>(), OnPageSelectListener
{
    override val layoutRes: Int = R.layout.main_view
    override val vm: MainViewModel by viewModel()
    override val mainLayout: View by lazy { binding.layoutViewPager }

    lateinit var tab1stBinding: ItemMainTabBinding
    lateinit var tab2ndBinding: ItemMainTabBinding
    lateinit var tab3rdBinding: ItemMainTabBinding

    private var inquiryTabIcon: Pair<Int, Int> = Pair(R.drawable.ic_activate_inquiry, R.drawable.ic_inactivate_inquiry)

    private val baseFragments =
        arrayListOf(RegisterMainFragment(), InquiryMainFragment(), MenuMainFragment())

    private val appPasswordRepo: AppPasswordRepository by inject()

    private val refreshParcelBroadcastReceiver = RefreshParcelBroadcastReceiver()

    private val _currentPage = MutableLiveData<Int>()
    val currentPage: LiveData<Int>
        get() = _currentPage

    lateinit var tabReselectListener: () -> Unit

    override fun onBeforeBinding()
    {
        PowerManager.checkWhiteList(this)
    }

    override fun onAfterBinding()
    {
        setViewPager()
        setTabLayout()
        checkInitializedTab()
        checkAppPassword()
    }

    override fun setObserve()
    {
        super.setObserve()

    }

    private fun checkInitializedTab() = CoroutineScope(Dispatchers.Default).launch {
        val clipBoardData = ClipboardUtil.pasteClipboardText(context = this@MainView)

        clipBoardData?.let {
            SopoLog.d("클립보드가 있습니다. [data:$it]")
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

    override fun onResume()
    {
        super.onResume()
        registerReceiver(refreshParcelBroadcastReceiver, IntentFilter().apply { addAction(RefreshParcelBroadcastReceiver.ACTION) })
    }

    override fun onPause()
    {
        super.onPause()
        unregisterReceiver(refreshParcelBroadcastReceiver)
    }

    private fun checkAppPassword()
    {
        runBlocking(Dispatchers.Default) { appPasswordRepo.get() } ?: return

        setOnActivityResultCallbackListener(object: OnActivityResultCallbackListener
        {
            override fun callback(activityResult: ActivityResult)
            {
                if(activityResult.resultCode == Activity.RESULT_CANCELED)
                {
                    finishAffinity()
                    return
                }
            }

        })

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
                _currentPage.postValue(tab?.position ?: 0)

                when(tab?.position)
                {
                    NavigatorConst.REGISTER_TAB -> activateTab(tab1stBinding, R.drawable.ic_activate_register)
                    NavigatorConst.INQUIRY_TAB -> activateTab(tab2ndBinding, inquiryTabIcon.first)
                    NavigatorConst.MY_MENU_TAB -> activateTab(tab3rdBinding, R.drawable.ic_activate_menu)
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?)
            {
                when(tab?.position)
                {
                    NavigatorConst.REGISTER_TAB -> inactivateTab(tab1stBinding, R.drawable.ic_inactivate_register)
                    NavigatorConst.INQUIRY_TAB -> inactivateTab(tab2ndBinding, inquiryTabIcon.second)
                    NavigatorConst.MY_MENU_TAB -> inactivateTab(tab3rdBinding, R.drawable.ic_inactivate_menu)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?)
            {


                when(tab?.position ?: return)
                {
                    NavigatorConst.REGISTER_TAB ->
                    {
//                        FragmentManager.remove(activity = this@MainView)
//                        TabCode.REGISTER_INPUT.FRAGMENT = InputParcelFragment.newInstance(null, 0)
//
//                        FragmentManager.move(activity = this@MainView, code = TabCode.REGISTER_INPUT, viewId = RegisterMainFragment.viewId)
                    }
                    NavigatorConst.INQUIRY_TAB ->
                    {
                        tabReselectListener.invoke()
//                        FragmentManager.remove(activity = this@MainView)
//                        TabCode.INQUIRY.FRAGMENT = InquiryFragment.newInstance(returnType = 0)
//                        FragmentManager.move(activity = this@MainView, code = TabCode.INQUIRY, viewId = InquiryMainFragment.viewId)
                    }
                    NavigatorConst.MY_MENU_TAB ->
                    {
//                        FragmentManager.move(activity = this@MainView, code = TabCode.MY_MENU_MAIN.apply {
//                            FRAGMENT = MenuFragment.newInstance()
//                        }, viewId = MenuMainFragment.viewId)
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
        binding.layoutMainTab.visibility = View.VISIBLE

//        binding.layoutViewPager.isUserInputEnabled = true
        binding.layoutViewPager.isUserInputEnabled = false
        binding.layoutMainTab.getTabAt(0)?.view?.visibility = View.VISIBLE
        binding.layoutMainTab.getTabAt(1)?.view?.visibility = View.VISIBLE
        binding.layoutMainTab.getTabAt(2)?.view?.visibility = View.VISIBLE
    }

    fun hideTab()
    {
        binding.layoutMainTab.visibility = View.GONE

        binding.layoutViewPager.isUserInputEnabled = false

        binding.layoutMainTab.getTabAt(0)?.view?.visibility = View.GONE
        binding.layoutMainTab.getTabAt(1)?.view?.visibility = View.GONE
        binding.layoutMainTab.getTabAt(2)?.view?.visibility = View.GONE
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

        if(currentPage.value == TabCode.secondTab)
        {
            tab2ndBinding.ivTab.background = ContextCompat.getDrawable(this,inquiryTabIcon.first)
        }
        else
        {
            tab2ndBinding.ivTab.background = ContextCompat.getDrawable(this,inquiryTabIcon.second)
        }
    }

    override fun onMoveToPage(page: Int)
    {
        binding.layoutViewPager.setCurrentItem(page, true)
    }


}