package com.delivery.sopo.views.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.consts.IntentConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.MainViewBinding
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.launchActivity
import com.delivery.sopo.services.PowerManager
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import com.delivery.sopo.viewmodels.main.MainViewModel
import com.delivery.sopo.viewmodels.menus.MenuMainFrame
import com.delivery.sopo.views.adapter.ViewPagerAdapter
import com.delivery.sopo.views.inquiry.InquiryMainFrame
import com.delivery.sopo.views.menus.LockScreenView
import com.delivery.sopo.views.menus.MenuFragment
import com.delivery.sopo.views.registers.RegisterMainFrame
import com.delivery.sopo.views.registers.InputParcelFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.tap_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainView: BasicView<MainViewBinding>(R.layout.main_view)
{
    private val vm: MainViewModel by viewModel()

    var currentPage = MutableLiveData<Int?>()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        // WhiteList 추가
        PowerManager.checkWhiteList(this)

        initUI()
    }

    override fun bindView()
    {
        binding.vm = vm
    }

    override fun setObserver()
    {
        /** 화면 패스워드 설정 **/
        vm.isSetAppPassword.observe(this, Observer { appPassword ->
            appPassword?.let {
                Intent(this@MainView, LockScreenView::class.java).apply {
                    putExtra(IntentConst.LOCK_SCREEN, LockScreenStatusEnum.VERIFY)
                }.launchActivity(this@MainView)
            }
        })

        SOPOApp.cntOfBeUpdate.observe(this, Observer { cnt ->

            if(cnt > 0)
            {
                SopoLog.d(msg = "업데이트 가능 택배 갯수[Size:$cnt]")

                binding.alertMessageBar.run {
                    setText("${cnt}개의 새로운 배송정보가 있어요.")
                    setTextColor(R.color.MAIN_WHITE)
                    setOnCancelClicked("업데이트", R.color.MAIN_WHITE, View.OnClickListener {

                        moveToSpecificTab(TabCode.secondTab)

                        CoroutineScope(Dispatchers.IO).launch {
                            vm.requestOngoingParcels()
                        }

                        onDismiss()
                    })
                    onStart()
                }

                //                SOPOApp.cntOfBeUpdate.postValue(null)
            }
        })

        SOPOApp.currentPage.observe(this, Observer {
            if(it == null)
            {
                return@Observer
            }

            SopoLog.d("Navigator >>> ${it}번")

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

    fun moveToSpecificTab(pos: Int)
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

    private fun initUI()
    {
        setViewPager()
        setTabLayout()
    }

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

                                             tab.customView?.iv_tab?.setBackgroundResource(res)
                                             tab.customView?.tv_tab_name?.setTextColor(
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

                                             tab.customView?.iv_tab?.setBackgroundResource(res)
                                             tab.customView?.tv_tab_name?.setTextColor(
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
            setCustomView(R.layout.tap_item)
            customView?.run {
                iv_tab.setBackgroundResource(iconRes)
                tv_tab_name.text = tabName
                tv_tab_name.setTextColor(ContextCompat.getColor(this@MainView, textColor))
            }
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
        CoroutineScope(Dispatchers.IO).launch {
            vm.requestOngoingParcels()
        }

    }

    override fun onResume()
    {
        super.onResume()

        SopoLog.d("Test!!!! on Resume")
    }

    override fun onPause()
    {
        super.onPause()

        SopoLog.d("TEST!!! onPause")
    }
}