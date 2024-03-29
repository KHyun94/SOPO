package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.util.SopoLog

class MenuSubViewModel:ViewModel()
{
    val navigator = MutableLiveData<String>()
    val title = MutableLiveData<String>()

    fun  onClearClicked(){
        SopoLog.d("onClearClicked() 호출")
        navigator.postValue(NavigatorConst.TO_BACK_SCREEN)
    }
}