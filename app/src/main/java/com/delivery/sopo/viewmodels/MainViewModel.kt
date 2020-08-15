package com.delivery.sopo.viewmodels

import android.util.Log
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.FragmentConst
import com.google.android.material.tabs.TabLayout

class MainViewModel : ViewModel()
{
    var currentTabName = MutableLiveData<String>()

    init
    {
        currentTabName.value = FragmentConst.FRAGMENT_REGISTER
    }

    val listener : TabLayout.OnTabSelectedListener = object : TabLayout.OnTabSelectedListener
    {
        override fun onTabSelected(tab: TabLayout.Tab)
        {

            //클릭하는 탭의 번호
            tab.icon!!.setTint(SOPOApp.INSTANCE.resources.getColor(R.color.COLOR_MAIN_BLUE))

            when(tab.position){
                0 -> currentTabName.value = FragmentConst.FRAGMENT_REGISTER
                1 -> currentTabName.value = FragmentConst.FRAGMENT_LOOKUP
                2 -> currentTabName.value = FragmentConst.FRAGMENT_MY_MENU
                else -> currentTabName.value = ""
            }

        }

        override fun onTabUnselected(tab: TabLayout.Tab)
        {
            tab.icon!!.setTint(SOPOApp.INSTANCE.resources.getColor(R.color.COLOR_GRAY_400))
        }

        override fun onTabReselected(tab: TabLayout.Tab)
        {
        }
    }
}