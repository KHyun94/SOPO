package com.delivery.sopo.bindings

import android.util.Log
import androidx.databinding.BindingAdapter
import androidx.viewpager.widget.ViewPager
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.util.adapters.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.main_view.view.*

object ViewPagerBindingAdapter
{
    // 200827_2 Setting ViewPager
    @JvmStatic
    @BindingAdapter("setAdapter", "setCurrentItem", "addOnPageChangeListener")
    fun bindSettingViewPager(
        vp: ViewPager,
        adapter: ViewPagerAdapter,
        currentItem: Int,
        listener: ViewPager.OnPageChangeListener
    )
    {
        vp.adapter = adapter
        vp.currentItem = currentItem

        vp.addOnPageChangeListener(listener)
    }

    // 200827_2 Setting Connect Between ViewPager And TabLayout
    @JvmStatic
    @BindingAdapter("setupWithViewPager")
    fun bindSetupWithViewPager(
        tl: TabLayout,
        vp: ViewPager
    )
    {
        tl.setupWithViewPager(vp)

    }

    //----------------------------------------------------------------------------------------------
}