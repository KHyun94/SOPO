package com.delivery.sopo.viewmodels.menus

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst

class AccountManagerViewModel: ViewModel()
{
    private val _navigator = MutableLiveData<String>()
    val navigator : LiveData<String>
    get() = _navigator

    fun onMoveClicked(v: View){
        when(v.id)
        {
            R.id.layout_update_nickname -> _navigator.value = NavigatorConst.TO_UPDATE_NICKNAME
            R.id.layout_reset_password -> _navigator.value = NavigatorConst.TO_RESET_PASSWORD
            R.id.layout_logout -> _navigator.value = NavigatorConst.TO_LOGOUT
            R.id.layout_sign_out -> _navigator.value = NavigatorConst.TO_SIGN_OUT
        }
    }
}