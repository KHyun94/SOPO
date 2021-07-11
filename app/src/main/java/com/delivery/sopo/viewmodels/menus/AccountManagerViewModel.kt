package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.MenuEnum
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.SopoLog
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

//    private suspend fun updateNickname(nickname: String): ResponseResult<String>
//    {
//        return when (val result = UserCall.updateNickname(nickname))
//        {
//            is NetworkResult.Success ->
//            {
//                userLocalRepository.setNickname(nickname)
//                SopoLog.d("Success to update nickname")
//                ResponseResult(true, null, nickname, "Success to update nickname")
//            }
//            is NetworkResult.Error ->
//            {
//                SopoLog.e("Fail to update nickname")
//                ResponseResult(false, null, "", "Fail to update nickname", DisplayEnum.DIALOG)
//            }
//        }
//    }
}