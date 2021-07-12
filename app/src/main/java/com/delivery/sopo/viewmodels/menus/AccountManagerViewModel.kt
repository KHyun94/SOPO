package com.delivery.sopo.viewmodels.menus

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.MenuEnum
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.SopoLog
import kotlinx.android.synthetic.main.fragment_account_manager.*

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