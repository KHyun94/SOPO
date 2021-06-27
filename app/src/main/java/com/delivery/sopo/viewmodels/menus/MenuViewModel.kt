package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.livedates.SingleLiveEvent

class MenuViewModel() : ViewModel()
{
    val menu = SingleLiveEvent<TabCode?>()

    fun onMoveToSubMenuClicked(code: TabCode){
        SopoLog.d("onMoveToSubMenuClicked() 호출 [TabCode:$code]")
        menu.postValue(code)
    }

    override fun onCleared()
    {
        super.onCleared()
       menu.postValue(null)
    }
}