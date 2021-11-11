package com.delivery.sopo.views.main

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.local.app_password.AppPasswordRepository
import com.delivery.sopo.databinding.ItemMainTabBinding
import com.delivery.sopo.databinding.MainViewBinding
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.services.PowerManager
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.viewmodels.main.MainViewModel
import com.delivery.sopo.viewmodels.menus.MenuMainFrame
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

class MainView: BaseView<MainViewBinding, MainViewModel>()
{
    override val layoutRes: Int = R.layout.main_view
    override val vm: MainViewModel by viewModel()
    override val mainLayout: View by lazy { binding.layoutViewPager }

    lateinit var tab1stBinding: ItemMainTabBinding
    lateinit var tab2ndBinding: ItemMainTabBinding
    lateinit var tab3rdBinding: ItemMainTabBinding

    private val baseFragments = arrayListOf(RegisterMainFragment(), InquiryMainFragment(), MenuMainFrame())

    private val appPasswordRepo: AppPasswordRepository by inject()

    var currentPage = MutableLiveData<Int?>()

    override fun onBeforeBinding()
    {
        PowerManager.checkWhiteList(this)
        checkAppPassword()
    }

    override fun onAfterBinding()
    {
        setViewPager()
        setTabLayout()
    }

    override fun setObserve()
    {
        super.setObserve()

        // TODO Parcel tab으로 이동해야 함
        /*SOPOApp.cntOfBeUpdate.observe(this, Observer { cnt ->
            if(cnt > 0)
            {
                SopoLog.d(msg = "업데이트 가능 택배 갯수[Size:$cnt]")

                binding.alertMessageBar.run {
                    setText("${cnt}개의 새로운 배송정보가 있어요.")
                    setTextColor(R.color.MAIN_WHITE)
                    setOnCancelClicked("업데이트", R.color.MAIN_WHITE, View.OnClickListener {

                        moveToSpecificTab(TabCode.secondTab)

                        GlobalScope.launch {
                            vm.requestOngoingParcels()
                        }

                        onDismiss()
                    })
                    onStart()
                }
            }
        })*/

        SOPOApp.currentPage.observe(this, Observer {
            if(it == null) return@Observer
            when(it)
            {
                NavigatorConst.REGISTER_TAB -> binding.layoutViewPager.setCurrentItem(NavigatorConst.REGISTER_TAB, true)
                NavigatorConst.INQUIRY_TAB -> binding.layoutViewPager.setCurrentItem(NavigatorConst.INQUIRY_TAB, true)
                NavigatorConst.MY_MENU_TAB -> binding.layoutViewPager.setCurrentItem(NavigatorConst.MY_MENU_TAB, true)
                else -> throw Exception("NO TAB")
            }

        })

    }

    private fun checkAppPassword()
    {
        runBlocking(Dispatchers.Default) { appPasswordRepo.get() } ?: return

        val intent = Intent(this@MainView, LockScreenView::class.java).apply {
            putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.VERIFY)
        }

        launchActivityResult(intent, ActivityResultCallback<ActivityResult> { result ->
            if(result.resultCode == Activity.RESULT_CANCELED)
            {
                finishAffinity()
                return@ActivityResultCallback
            }
        })
    }

    /*    private fun refreshTokenWithinWeek()
        {
             val oAuth =  oAuthLocalRepo.get(userLocalRepo.getUserId())

            val isDate =
                DateUtil.isExpiredDateWithinAWeek(SOPOApp.oAuth?.refreshTokenExpiredAt ?: return)
            if(isDate) SOPOWorkManager.refreshOAuthWorkManager(this)
        }*/


    fun getAlertMessageBar() = binding.alertMessageBar

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
        val adapter = ViewPagerAdapter(this, baseFragments)

        binding.layoutViewPager.adapter = adapter

        val onTabSelectedListener = object: TabLayout.OnTabSelectedListener
        {
            override fun onTabSelected(tab: TabLayout.Tab?)
            {
                currentPage.postValue(tab?.position)

                when(tab?.position)
                {
                    NavigatorConst.REGISTER_TAB -> activateTab(tab1stBinding, R.drawable.ic_activate_register)
                    NavigatorConst.INQUIRY_TAB -> activateTab(tab2ndBinding, R.drawable.ic_activate_inquiry)
                    NavigatorConst.MY_MENU_TAB -> activateTab(tab3rdBinding, R.drawable.ic_activate_menu)
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?)
            {
                when(tab?.position)
                {
                    NavigatorConst.REGISTER_TAB -> inactivateTab(tab1stBinding, R.drawable.ic_inactivate_register)
                    NavigatorConst.INQUIRY_TAB -> inactivateTab(tab2ndBinding, R.drawable.ic_inactivate_inquiry)
                    NavigatorConst.MY_MENU_TAB -> inactivateTab(tab3rdBinding, R.drawable.ic_inactivate_menu)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?)
            {
                if(tab == null) return

                when(tab.position)
                {
                    NavigatorConst.REGISTER_TAB ->
                    {
                        FragmentManager.remove(activity = this@MainView)
                        TabCode.REGISTER_INPUT.FRAGMENT = InputParcelFragment.newInstance(null, 0)

                        FragmentManager.move(activity = this@MainView, code = TabCode.REGISTER_INPUT, viewId = RegisterMainFragment.viewId)
                    }
                    NavigatorConst.INQUIRY_TAB ->
                    {
                        FragmentManager.remove(activity = this@MainView)
                        FragmentManager.move(activity = this@MainView, code = TabCode.INQUIRY, viewId = InquiryMainFragment.viewId)
                    }
                    NavigatorConst.MY_MENU_TAB ->
                    {
                        FragmentManager.move(activity = this@MainView, code = TabCode.MY_MENU_MAIN.apply {
                            FRAGMENT = MenuFragment.newInstance()
                        }, viewId = MenuMainFrame.viewId)
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
            0 -> tab1stBinding =
                setTabIcon(tab, R.drawable.ic_activate_register, "등록", R.color.COLOR_MAIN_700)
            1 -> tab2ndBinding =
                setTabIcon(tab, R.drawable.ic_inactivate_inquiry, "조회", R.color.COLOR_GRAY_400)
            2 -> tab3rdBinding =
                setTabIcon(tab, R.drawable.ic_inactivate_menu, "메뉴", R.color.COLOR_GRAY_400)
        }
    }

    private fun setTabIcon(tab: TabLayout.Tab,
                           @DrawableRes iconRes: Int, tabName: String, textColor: Int): ItemMainTabBinding
    {
        val tabBinding =
            ItemMainTabBinding.bind(tab.customView ?: throw NullPointerException("TAB is null"))

        tabBinding.ivTab.setBackgroundResource(iconRes)
        tabBinding.tvTabName.text = tabName
        tabBinding.tvTabName.setTextColor(ContextCompat.getColor(this@MainView, textColor))

        return tabBinding
    }

    fun onCompleteRegister()
    {
        binding.layoutViewPager.currentItem = 1
        /*GlobalScope.launch {
            vm.requestOngoingParcels()
        }*/

    }
}