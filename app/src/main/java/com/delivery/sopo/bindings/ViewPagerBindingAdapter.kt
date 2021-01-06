package com.delivery.sopo.bindings

import androidx.databinding.BindingAdapter
import androidx.viewpager.widget.ViewPager
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.views.adapter.ViewPagerAdapter
import com.delivery.sopo.views.main.MainView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.tap_item.view.*

object ViewPagerBindingAdapter
{
    @JvmStatic
    @BindingAdapter("setViewPagerAdapter")
    fun bindViewPager(vp: ViewPager, adapter: ViewPagerAdapter)
    {
        vp.adapter = adapter
        vp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener
        {
            override fun onPageScrollStateChanged(p0: Int)
            {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int)
            {
            }

            override fun onPageSelected(p0: Int)
            {
                // 0923 kh 등록 성공
                if (p0 == 1 && MainView.isRegister)
                {
                }
            }
        })
    }

    @JvmStatic
    @BindingAdapter("setLinkViewPager")
    fun bindTabLayoutSetting(tl: TabLayout, vp: ViewPager)
    {
        SopoLog.d(msg = "setLinkVP - start")

        tl.setupWithViewPager(vp)

        tabSetting(tl)

        SopoLog.d(msg = "setLinkVP - middle")


        tl.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener
        {
            override fun onTabSelected(tab: TabLayout.Tab?)
            {
                SopoLog.d(msg = "Tab Click!!! ====> ${tab?.position}")
                val res = when (tab?.position)
                {
                    NavigatorConst.REGISTER_TAB -> R.drawable.ic_activate_register
                    NavigatorConst.INQUIRY_TAB -> R.drawable.ic_activate_inquiry
                    NavigatorConst.MY_MENU_TAB -> R.drawable.ic_activate_menu
                    else -> NavigatorConst.REGISTER_TAB
                }

                SopoLog.d(msg = "tabSetting ${tab!!.parent!!.getTabAt(tab.position)!!.customView} ")


//                tab?.view?.iv_tab!!.iv_tab.setBackgroundResource(res)
//                tab.view.iv_tab!!.tv_tab_name.setTextColor(SOPOApp.INSTANCE.resources.getColor(R.color.COLOR_MAIN_BLUE_700))
            }

            override fun onTabUnselected(tab: TabLayout.Tab?)
            {
                val res = when (tab!!.position)
                {
                    NavigatorConst.REGISTER_TAB -> R.drawable.ic_inactivate_register
                    NavigatorConst.INQUIRY_TAB -> R.drawable.ic_inactivate_inquiry
                    NavigatorConst.MY_MENU_TAB -> R.drawable.ic_inactivate_menu
                    else -> NavigatorConst.REGISTER_TAB
                }

//                tab.customView!!.iv_tab.setBackgroundResource(res)
//                tab.customView!!.tv_tab_name.setTextColor(SOPOApp.INSTANCE.resources.getColor(R.color.COLOR_GRAY_400))
            }

            override fun onTabReselected(tab: TabLayout.Tab?)
            {
            }
        })



        SopoLog.d(msg = "setLinkVP - end ${tl.tabCount}")

    }

    fun tabSetting(tl: TabLayout)
    {
        SopoLog.d(msg = "tabSetting ${tl.tabCount}  ${tl}")

        tl.addTab(tl.newTab().setCustomView(R.layout.tap_item), 0, true)
        tl.addTab(tl.newTab().setCustomView(R.layout.tap_item), 1, false)
        tl.addTab(tl.newTab().setCustomView(R.layout.tap_item), 2, false)


        // layout을 dynamic 처리해서 넣도록 수정
        tl.getTabAt(NavigatorConst.REGISTER_TAB)!!.run {
            customView!!.run {
                iv_tab.setBackgroundResource(R.drawable.ic_activate_register)
                tv_tab_name.setText("등록")
                tv_tab_name.setTextColor(resources.getColor(R.color.COLOR_MAIN_BLUE_700))
            }
        }
        if(tl.getTabAt(0)!!.customView == null) SopoLog.d(msg = "없지 왜? !ㅋㅋㅋㅋㅋㅋ")
        else SopoLog.d(msg = "있는데 왜? ㅋㅋㅋ ${tl.getTabAt(0)!!.customView!!.tv_tab_name.text.toString()}")
        tl.getTabAt(NavigatorConst.INQUIRY_TAB)!!.run {
            customView!!.run {
                iv_tab.setBackgroundResource(R.drawable.ic_inactivate_inquiry)
                tv_tab_name.setText("조회")
                tv_tab_name.setTextColor(resources.getColor(R.color.COLOR_GRAY_400))
            }
        }

        tl.getTabAt(NavigatorConst.MY_MENU_TAB)!!.run {
            customView!!.run {
                iv_tab.setBackgroundResource(R.drawable.ic_inactivate_menu)
                tv_tab_name.setText("메뉴")
                tv_tab_name.setTextColor(resources.getColor(R.color.COLOR_GRAY_400))
            }
        }

        SopoLog.d(msg = "tabSetting ${tl.tabCount}")
    }
}