package com.delivery.sopo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel()
{
    val tabLayoutVisibility = MutableLiveData<Int>()

    fun setTabLayoutVisiblity(visibility: Int){
        tabLayoutVisibility.value = visibility
    }
}