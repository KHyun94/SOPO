package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.util.SopoLog

class MenuSubViewModel:ViewModel()
{
    private var _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    init
    {
        _navigator.postValue(TabCode.REGISTER_INPUT.NAME)
    }
    fun  onClearClicked(){
        SopoLog.d("onClearClicked() 호출")
        _navigator.postValue(NavigatorConst.TO_BACK_SCREEN)
    }
}