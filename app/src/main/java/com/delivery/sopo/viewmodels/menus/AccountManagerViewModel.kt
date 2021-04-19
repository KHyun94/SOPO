package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.enums.MenuEnum
import kotlinx.android.synthetic.main.fragment_account_manager.*

class AccountManagerViewModel: ViewModel()
{
    private val _navigator = MutableLiveData<MenuEnum>()
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
        _navigator.postValue(MenuEnum.SIGN_OUT)
    }

}