package com.delivery.sopo.views.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity, fragments:ArrayList<Fragment>) : FragmentStateAdapter(fragmentActivity)
{
    private var items: ArrayList<Fragment> = fragments

    override fun getItemCount(): Int
    {
        return items.size
    }

    override fun createFragment(position: Int): Fragment
    {
        return items[position]
    }
    /*   var currentFragment: Fragment

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
   //                tab1stFragment
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
       }*/
}