package com.delivery.sopo.presentation.viewmodels.menus

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.R
import com.delivery.sopo.domain.usecase.user.UpdateNicknameUseCase
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.domain.usecase.user.token.LogoutUseCase
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountManagerViewModel(
        private val updateNicknameUseCase: UpdateNicknameUseCase,
        private val logoutUseCase: LogoutUseCase
): BaseViewModel()
{
    private val _navigator = MutableLiveData<String>()
    val navigator : LiveData<String> = _navigator

    private val _nickname = MutableLiveData<String>()
    val nickname : LiveData<String> = _nickname

    init
    {
        updateNickname()
    }

    fun updateNickname() = viewModelScope.launch(Dispatchers.Default){
        _nickname.postValue(updateNicknameUseCase.getUserNickname())
    }

    fun onUpdateNickname(nickname: String) = scope.launch {
        onStartLoading()
        updateNicknameUseCase(nickname = nickname)
        updateNickname()
        _navigator.postValue("UPDATE_COMPLETED")
        onStopLoading()
    }

    fun onLogout() = scope.launch {
        logoutUseCase.invoke()
    }

    fun onBackClicked(){
        _navigator.postValue(NavigatorConst.Event.BACK)
    }

    fun onMoveClicked(v: View){
        when(v.id)
        {
            R.id.layout_update_nickname -> _navigator.value = NavigatorConst.Screen.UPDATE_NICKNAME
            R.id.layout_reset_password -> _navigator.value = NavigatorConst.Screen.RESET_PASSWORD
            R.id.layout_logout -> _navigator.value = NavigatorConst.TO_LOGOUT
            R.id.layout_sign_out -> _navigator.value = NavigatorConst.TO_SIGN_OUT
        }
    }
}