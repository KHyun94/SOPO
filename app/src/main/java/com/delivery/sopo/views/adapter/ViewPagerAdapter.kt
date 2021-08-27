package com.delivery.sopo.views.adapter

import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.menus.MenuMainFrame
import com.delivery.sopo.views.inquiry.InquiryMainFrame
import com.delivery.sopo.views.registers.RegisterMainFrame

class ViewPagerAdapter(fm: FragmentManager, val pageCnt: Int) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
{
    var currentFragment: Fragment

    var tab1stFragment: Fragment = RegisterMainFrame()
    var tab2ndFragment: Fragment = InquiryMainFrame()
    var tab3rdFragment: Fragment = MenuMainFrame()

    var data: Bundle? = null

    init
    {
        currentFragment = tab1stFragment
    }

    override fun getPageTitle(position: Int): CharSequence?
    {
        return super.getPageTitle(position)
    }

    override fun getItem(position: Int): Fragment
    {
        return when (position)
        {
            NavigatorConst.REGISTER_TAB ->
            {
                tab1stFragment
            }
            NavigatorConst.INQUIRY_TAB ->
            {
                tab2ndFragment
            }
            NavigatorConst.MY_MENU_TAB ->
            {
                tab3rdFragment
            }
            else ->
            {
                tab1stFragment
            }
        }
    }

    fun nextFragment()
    {

    }


    override fun getCount(): Int
    {
        return pageCnt
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any)
    {
        // 200827 프래그먼트 갱신 OFF
//        super.destroyItem(container, position, `object`)
    }
}