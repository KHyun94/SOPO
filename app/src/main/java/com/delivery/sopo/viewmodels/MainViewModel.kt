package com.delivery.sopo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.database.room.AppDatabase

class MainViewModel: ViewModel()
{
    val tabLayoutVisibility = MutableLiveData<Int>()

    fun setTabLayoutVisiblity(visibility: Int){
        tabLayoutVisibility.value = visibility
    }
}