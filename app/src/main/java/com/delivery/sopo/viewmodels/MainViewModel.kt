package com.delivery.sopo.viewmodels

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewpager.widget.ViewPager
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.enums.FragmentType
import com.delivery.sopo.util.adapters.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout

class MainViewModel(var contract: MainActivityContract) : ViewModel()
{
    val TAG = "LOG.SOPO.MVM"

    interface MainActivityContract {
        fun getFragmentManger(): FragmentManager
    }

//    lateinit var contract: MainActivityContract


    var adapter : ViewPagerAdapter
    var currItem = 0

    var currentTabName = MutableLiveData<String>()
    var current1stTabName = MutableLiveData<String>()
    var current2ndTabName = MutableLiveData<String>()
    var current3rdTabName = MutableLiveData<String>()

    init
    {
        adapter = ViewPagerAdapter(contract.getFragmentManger(), 3)

        currentTabName.value = FragmentType.REGISTER_STEP1.NAME

        current1stTabName.value = FragmentType.REGISTER_STEP1.NAME
        current2ndTabName.value = FragmentType.INQUIRY.NAME
        current3rdTabName.value = FragmentType.MY_MENU.NAME
    }

    var pageChangeListener = object : ViewPager.OnPageChangeListener
    {

        override fun onPageScrollStateChanged(p0: Int)
        {
        }

        override fun onPageScrolled(p0: Int, p1: Float, p2: Int)
        {
        }

        override fun onPageSelected(p0: Int)
        {
            currItem = p0
            Log.d(TAG, "ViewPager Po => ${currItem}")
        }
    }
}