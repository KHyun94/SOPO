package com.delivery.sopo.presentation.viewmodels.login

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.domain.usecase.user.token.LoginUseCase
import kotlinx.coroutines.*

class LoginViewModel(private val loginUseCase: LoginUseCase): BaseViewModel()
{
    val email = MutableLiveData<String>()
    var password = MutableLiveData<String>()

    val validity = mutableMapOf<InfoEnum, Boolean>()

    private var _invalidity = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val invalidity: LiveData<Pair<InfoEnum, Boolean>>
        get() = _invalidity

    private var _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: MutableLiveData<Triple<View, Boolean, InfoEnum>>
        get() = _focus

    val focusChangeCallback: FocusChangeCallback = FocusChangeCallback@{ v, hasFocus, type ->
        _focus.value = (Triple(v, hasFocus, type))
    }

    init
    {
        validity[InfoEnum.EMAIL] = false
        validity[InfoEnum.PASSWORD] = false
    }

    override var onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onLoginError(error: ErrorCode)
        {
            super.onLoginError(error)
            postErrorSnackBar("유효한 이메일 또는 비밀번호가 아닙니다.")
        }

        override fun onFailure(error: ErrorCode)
        {
            when(error)
            {
                ErrorCode.NICK_NAME_NOT_FOUND -> _navigator.postValue(NavigatorConst.TO_UPDATE_NICKNAME)
                else -> postErrorSnackBar("로그인이 실패했습니다. 다시 시도해주세요.[${error.toString()}]")
            }
        }

        override fun onInternalServerError(error: ErrorCode)
        {
            super.onInternalServerError(error)
            postErrorSnackBar("일시적으로 서비스를 이용할 수 없습니다.")
        }

        override fun onAuthError(error: ErrorCode)
        {
            super.onAuthError(error)
            postErrorSnackBar("로그인이 실패했습니다. 다시 시도해주세요.[${error.toString()}]")
        }
    }

    fun onLoginClicked() = checkEventStatus(checkNetwork = true) {

        validity.forEach { (k, v) ->
            if(!v) return@checkEventStatus _invalidity.postValue(Pair(k, v))
        }

        requestLoginBySelf()
    }

    fun onResetPasswordClicked()
    {
        _navigator.postValue(NavigatorConst.TO_RESET_PASSWORD)
    }

    private fun requestLoginBySelf() = scope.launch(coroutineExceptionHandler) {
        try
        {
            onStartLoading()
            loginUseCase.invoke(username = email.value.toString(), password = password.value.toString().toMD5())
            _navigator.postValue(NavigatorConst.TO_MAIN)
        }
        finally
        {
            onStopLoading()
        }
    }
}
