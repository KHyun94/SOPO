package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.enums.TabCode

class MenuSubViewModel:ViewModel()
{
    val tabCode= MutableLiveData<TabCode?>()
}