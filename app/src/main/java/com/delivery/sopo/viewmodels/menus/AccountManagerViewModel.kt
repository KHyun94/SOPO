package com.delivery.sopo.viewmodels.menus

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.R
import com.delivery.sopo.exceptions.UserExceptionHandler
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.usecase.LogoutUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers

class AccountManagerViewModel(
        private val logoutUseCase: LogoutUseCase
): BaseViewModel()
{
    private val _navigator = MutableLiveData<String>()
    val navigator : LiveData<String>
    get() = _navigator

    fun onLogout(){
        logoutUseCase.invoke()
    }

    fun onBackClicked(){
        _navigator.postValue(NavigatorConst.TO_BACK_SCREEN)
    }

    fun onMoveClicked(v: View){
        when(v.id)
        {
            R.id.layout_update_nickname -> _navigator.value = NavigatorConst.TO_UPDATE_NICKNAME
            R.id.layout_reset_password -> _navigator.value = NavigatorConst.TO_RESET_PASSWORD
            R.id.layout_logout -> _navigator.value = NavigatorConst.TO_LOGOUT
            R.id.layout_sign_out -> _navigator.value = NavigatorConst.TO_SIGN_OUT
        }
    }

    override var onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onFailure(error: ErrorEnum) { }
    }

    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        UserExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }
}