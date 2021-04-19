package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.enums.MenuEnum

class AccountManagerViewModel: ViewModel()
{
    val _navigator = MutableLiveData<MenuEnum>()
    val navigator : LiveData<MenuEnum>
    get() = _navigator

    fun onUpdateNicknameClicked()
    {
        _navigator.value = MenuEnum.UPDATE_NICKNAME
    }

    fun onLogoutClicked(){

    }

    fun onBackUpClicked()
    {

    }

    fun onSignOutClicked()
    {
        _navigator.value = MenuEnum.SIGN_OUT
    }

}