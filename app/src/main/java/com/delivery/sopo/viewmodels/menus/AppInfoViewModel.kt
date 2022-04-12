package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.models.base.BaseViewModel

class AppInfoViewModel : BaseViewModel() {

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    fun postNavigator(navigator: String){
        _navigator.postValue(navigator)
    }

    fun onBackClicked(){
        _navigator.postValue(NavigatorConst.TO_BACK_SCREEN)
    }
}