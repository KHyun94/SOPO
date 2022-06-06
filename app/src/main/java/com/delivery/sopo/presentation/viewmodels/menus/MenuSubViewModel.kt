package com.delivery.sopo.presentation.viewmodels.menus

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.util.SopoLog

class MenuSubViewModel:ViewModel()
{
    val navigator = MutableLiveData<String>()
    val title = MutableLiveData<String>()

    fun  onClearClicked(){
        SopoLog.d("onClearClicked() 호출")
        navigator.postValue(NavigatorConst.Event.BACK)
    }
}