package com.delivery.sopo.bindings

import android.util.Log
import androidx.databinding.BindingAdapter
import androidx.viewpager.widget.ViewPager
import com.delivery.sopo.R
import com.delivery.sopo.views.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout

object ViewPagerBindingAdapter
{
    // 200827_2 Setting ViewPager
    @JvmStatic
    @BindingAdapter("setupWithViewPager", "setAdapter", "setCurrentItem", "addOnPageChangeListener")
    fun bindSettingViewPager(
        vp: ViewPager,
        tl: TabLayout,
        adapter: ViewPagerAdapter,
        currentItem: Int,
        listener: ViewPager.OnPageChangeListener
    )
    {
        vp.adapter = adapter
        vp.currentItem = currentItem

        vp.addOnPageChangeListener(listener)

        tl.setupWithViewPager(vp)

        tl.getTabAt(0)!!.setIcon(R.drawable.ic_clicked_tap_register)
        tl.getTabAt(1)!!.setIcon(R.drawable.ic_non_clicked_tap_lookup)
        tl.getTabAt(2)!!.setIcon(R.drawable.ic_non_clicked_tap_my)

        tl.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener
        {
            override fun onTabSelected(tab: TabLayout.Tab?)
            {
                val res = when (tab!!.position)
                {
                    0 -> R.drawable.ic_clicked_tap_register
                    1 -> R.drawable.ic_clicked_tap_lookup
                    2 -> R.drawable.ic_clicked_tap_my
                    else -> R.drawable.ic_clicked_tap_register
                }

                tab.setIcon(res)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?)
            {
                val res = when (tab!!.position)
                {
                    0 -> R.drawable.ic_non_clicked_tap_register
                    1 -> R.drawable.ic_non_clicked_tap_lookup
                    2 -> R.drawable.ic_non_clicked_tap_my
                    else -> R.drawable.ic_non_clicked_tap_register
                }

                tab.setIcon(res)
            }

            override fun onTabReselected(tab: TabLayout.Tab?)
            {
            }

        })


        Log.d("LOG.SOPO", "2. 탭 사이즈 ${tl.tabCount}")
    }

    // 200827_2 Setting Connect Between ViewPager And TabLayout
//    @JvmStatic
//    @BindingAdapter("setupWithViewPager")
//    fun bindSetupWithViewPager(
//        tl: TabLayout,
//        vp: ViewPager
//    )
//    {
//        tl.setupWithViewPager(vp)
//    }

    //----------------------------------------------------------------------------------------------
}