package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.enums.TabCode

class MenuViewModel() : ViewModel()
{
    private val _menu = MutableLiveData<TabCode>()
    val menu: LiveData<TabCode>
        get() = _menu

    fun onMoveToSubMenuClicked(code: TabCode){
        _menu.postValue(code)
    }
}