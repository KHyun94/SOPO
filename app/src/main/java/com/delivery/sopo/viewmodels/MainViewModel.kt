package com.delivery.sopo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.enums.FragmentType
import com.google.android.material.tabs.TabLayout

class MainViewModel : ViewModel()
{
    var currentTabName = MutableLiveData<String>()
    var current1stTabName = MutableLiveData<String>()
    var current2ndTabName = MutableLiveData<String>()
    var current3rdTabName = MutableLiveData<String>()

    init
    {
        currentTabName.value = FragmentType.REGISTER_STEP1.NAME

        current1stTabName.value = FragmentType.REGISTER_STEP1.NAME
        current2ndTabName.value = FragmentType.LOOKUP.NAME
        current3rdTabName.value = FragmentType.MY_MENU.NAME
    }

    val listener : TabLayout.OnTabSelectedListener = object : TabLayout.OnTabSelectedListener
    {
        override fun onTabSelected(tab: TabLayout.Tab)
        {
            //클릭하는 탭의 번호
            tab.icon!!.setTint(SOPOApp.INSTANCE.resources.getColor(R.color.COLOR_MAIN_BLUE))

            when(tab.position){
                0 -> currentTabName.value = current1stTabName.value
                1 -> currentTabName.value = current2ndTabName.value
                2 -> currentTabName.value = current3rdTabName.value
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