package com.delivery.sopo.presentation.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.models.base.BaseViewModel

class NoticeViewModel : BaseViewModel()
{
    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    fun postNavigator(navigator: String){
        _navigator.postValue(navigator)
    }

    fun onBackClicked(){
        postNavigator(NavigatorConst.TO_BACK_SCREEN)
    }
}