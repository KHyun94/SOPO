package com.delivery.sopo.presentation.views.adapter

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

    override fun getItemId(position: Int): Long
    {
        return super.getItemId(position)
    }
}