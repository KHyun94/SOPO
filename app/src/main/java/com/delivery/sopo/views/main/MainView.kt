package com.delivery.sopo.views.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.database.room.dto.AppPasswordDTO
import com.delivery.sopo.data.repository.local.app_password.AppPasswordRepository
import com.delivery.sopo.databinding.ItemMainTabBinding
import com.delivery.sopo.databinding.MainViewBinding
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.models.base.ProcessInterface
import com.delivery.sopo.services.PowerManager
import com.delivery.sopo.services.workmanager.SOPOWorkManager
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.main.MainViewModel
import com.delivery.sopo.viewmodels.menus.MenuMainFrame
import com.delivery.sopo.views.adapter.ViewPagerAdapter
import com.delivery.sopo.views.inquiry.InquiryMainFrame
import com.delivery.sopo.views.menus.LockScreenView
import com.delivery.sopo.views.menus.MenuFragment
import com.delivery.sopo.views.registers.InputParcelFragment
import com.delivery.sopo.views.registers.RegisterMainFrame
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainView: BaseView<MainViewBinding, MainViewModel>()
{
    override val layoutRes: Int = R.layout.main_view
    override val vm: MainViewModel by viewModel()

    override fun receivedData(intent: Intent)
    {

    }

    private val appPasswordRepo: AppPasswordRepository by inject()

    var currentPage = MutableLiveData<Int?>()

    override fun onStart()
    {
        super.onStart()


    }

    override fun initUI()
    {
        setViewPager()
        setTabLayout()
    }

    override fun setAfterSetUI()
    {
        PowerManager.checkWhiteList(this)
        refreshTokenWithinWeek()

        checkAppPassword()
    }


    override fun setObserve()
    {
        SOPOApp.cntOfBeUpdate.observe(this, Observer { cnt ->
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
        })

        SOPOApp.currentPage.observe(this, Observer {
            if(it == null) return@Observer
            when(it)
            {
                NavigatorConst.REGISTER_TAB -> binding.layoutViewPager.setCurrentItem(
                    NavigatorConst.REGISTER_TAB, true)
                NavigatorConst.INQUIRY_TAB -> binding.layoutViewPager.setCurrentItem(
                    NavigatorConst.INQUIRY_TAB, true)
                NavigatorConst.MY_MENU_TAB -> binding.layoutViewPager.setCurrentItem(
                    NavigatorConst.MY_MENU_TAB, true)
                else -> throw Exception("NO TAB")
            }

        })

    }

    private fun checkAppPassword()
    {
        runBlocking(Dispatchers.Default) {  appPasswordRepo.get() } ?: return
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

    private fun refreshTokenWithinWeek()
    {
        val isDate =
            DateUtil.isExpiredDateWithinAWeek(SOPOApp.oAuth?.refreshTokenExpiredAt ?: return)
        if(isDate) SOPOWorkManager.refreshOAuthWorkManager(this)
    }

    private fun moveToSpecificTab(pos: Int)
    {
        SopoLog.d("moveToSpecificTab([pos:$pos]) 호출")

        when(pos)
        {
            NavigatorConst.REGISTER_TAB ->
            {
                SopoLog.d("Click For Update at RegisterTab")
                binding.layoutViewPager.currentItem = 1
            }
            NavigatorConst.INQUIRY_TAB ->
            {
                SopoLog.d("Click For Update at InquiryTab")
            }
            NavigatorConst.MY_MENU_TAB ->
            {
                SopoLog.d("Click For Update at MenuTab")
                binding.layoutViewPager.currentItem = 1
            }
        }
    }

    fun getAlertMessageBar() = binding.alertMessageBar

    private fun setTabLayout()
    {
        binding.layoutMainTab.run {
            setupWithViewPager(binding.layoutViewPager)
            setTabIcons(this)
            addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener
                                     {
                                         override fun onTabSelected(tab: TabLayout.Tab?)
                                         {
                                             tab ?: return


                                             currentPage.postValue(tab.position)

                                             val res = when(tab.position)
                                             {
                                                 NavigatorConst.REGISTER_TAB -> R.drawable.ic_activate_register
                                                 NavigatorConst.INQUIRY_TAB -> R.drawable.ic_activate_inquiry
                                                 NavigatorConst.MY_MENU_TAB -> R.drawable.ic_activate_menu
                                                 else -> NavigatorConst.REGISTER_TAB
                                             }

                                             val tabBinding =
                                                 ItemMainTabBinding.bind(tab.customView ?: return)

                                             tabBinding.ivTab.setBackgroundResource(res)
                                             tabBinding.tvTabName.setTextColor(
                                                 ContextCompat.getColor(this@MainView,
                                                                        R.color.COLOR_MAIN_700))
                                         }

                                         override fun onTabUnselected(tab: TabLayout.Tab?)
                                         {
                                             tab ?: return

                                             val res = when(tab.position)
                                             {
                                                 NavigatorConst.REGISTER_TAB -> R.drawable.ic_inactivate_register
                                                 NavigatorConst.INQUIRY_TAB -> R.drawable.ic_inactivate_inquiry
                                                 NavigatorConst.MY_MENU_TAB -> R.drawable.ic_inactivate_menu
                                                 else -> NavigatorConst.REGISTER_TAB
                                             }

                                             val tabBinding = ItemMainTabBinding.inflate(LayoutInflater.from(context))

                                             tabBinding.ivTab.setBackgroundResource(res)
                                             tabBinding.tvTabName.setTextColor(
                                                 ContextCompat.getColor(this@MainView,
                                                                        R.color.COLOR_GRAY_400))
                                         }

                                         override fun onTabReselected(tab: TabLayout.Tab?)
                                         {
                                             if(tab == null) return



                                             when(tab.position)
                                             {
                                                 NavigatorConst.REGISTER_TAB ->
                                                 {
                                                     FragmentManager.remove(
                                                         activity = this@MainView)
                                                     TabCode.REGISTER_INPUT.FRAGMENT =
                                                         InputParcelFragment.newInstance(null, 0)

                                                     FragmentManager.move(activity = this@MainView,
                                                                          code = TabCode.REGISTER_INPUT,
                                                                          viewId = RegisterMainFrame.viewId)
                                                 }
                                                 NavigatorConst.INQUIRY_TAB ->
                                                 {
                                                     FragmentManager.remove(
                                                         activity = this@MainView)
                                                     FragmentManager.move(activity = this@MainView,
                                                                          code = TabCode.INQUIRY,
                                                                          viewId = InquiryMainFrame.viewId)
                                                 }
                                                 NavigatorConst.MY_MENU_TAB ->
                                                 {
                                                     FragmentManager.move(activity = this@MainView,
                                                                          code = TabCode.MY_MENU_MAIN.apply {
                                                                              FRAGMENT =
                                                                                  MenuFragment.newInstance()
                                                                          },
                                                                          viewId = MenuMainFrame.viewId)
                                                 }
                                             }
                                         }
                                     })
        }
    }

    private fun setViewPager()
    {
        val addOnPageListener = object: ViewPager.OnPageChangeListener
        {
            override fun onPageScrollStateChanged(p0: Int)
            {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int)
            {
            }

            override fun onPageSelected(pageNum: Int)
            { // TODO 등록 성공 시 조회 페이지로 이동 처리
            }
        }

        binding.layoutViewPager.also { vp ->
            vp.adapter = ViewPagerAdapter(supportFragmentManager, 3)
        }.addOnPageChangeListener(addOnPageListener)
    }

    private fun setTabIcon(v: TabLayout, index: Int,
                           @DrawableRes iconRes: Int, tabName: String, textColor: Int)
    {
        v.getTabAt(index)?.run {
            setCustomView(R.layout.item_main_tab)

            val tabBinding = ItemMainTabBinding.bind(customView ?: return)

            tabBinding.ivTab.setBackgroundResource(iconRes)
            tabBinding.tvTabName.text = tabName
            tabBinding.tvTabName.setTextColor(ContextCompat.getColor(this@MainView, textColor))
        }
    }

    private fun setTabIcons(v: TabLayout)
    {
        setTabIcon(v, NavigatorConst.REGISTER_TAB, R.drawable.ic_activate_register, "등록",
                   R.color.COLOR_MAIN_700)
        setTabIcon(v, NavigatorConst.INQUIRY_TAB, R.drawable.ic_inactivate_inquiry, "조회",
                   R.color.COLOR_GRAY_400)
        setTabIcon(v, NavigatorConst.MY_MENU_TAB, R.drawable.ic_inactivate_menu, "메뉴",
                   R.color.COLOR_GRAY_400)
    }

    fun onCompleteRegister()
    {
        binding.layoutViewPager.currentItem = 1
        GlobalScope.launch {
            vm.requestOngoingParcels()
        }

    }
}