package com.delivery.sopo.presentation.login

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.presentation.bindings.FocusChangeCallback
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.domain.usecase.user.token.LoginUseCase
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.SOPOApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val loginUseCase: LoginUseCase): BaseViewModel()
{
    val username= MutableLiveData<String>()
    val password= MutableLiveData<String>()

    private var _navigator = MutableLiveData<String>()
    val navigator: LiveData<String> = _navigator

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: LiveData<Triple<View, Boolean, InfoEnum>> = _focus

    val focusChangeCallback: FocusChangeCallback = FocusChangeCallback@{ v, hasFocus, type ->
        _focus.value = Triple(v, hasFocus, type)
    }

    fun onLoginClicked() = checkEventStatus(checkNetwork = true) {
        requestLoginBySelf()
    }

    fun onResetPasswordClicked()
    {
        username.postValue("")
        password.postValue("")
        _navigator.postValue(NavigatorConst.Screen.RESET_PASSWORD)
    }

    private fun requestLoginBySelf() = scope.launch {
        try
        {
            onStartLoading()

            val username = username.value.toString()
            val password = password.value.toString().toMD5()

            loginUseCase(username = username, password = password)

            _navigator.postValue(NavigatorConst.Screen.MAIN)
        }
        finally
        {
            onStopLoading()
        }
    }

    override fun handlerAPIException(exception: SOPOApiException)
    {
        super.handlerAPIException(exception)
        when(exception.code)
        {
            ErrorCode.VALIDATION -> postErrorSnackBar(exception.message)
            ErrorCode.USER_NOT_FOUND -> postErrorSnackBar("계정 정보를 찾을 수 없습니다.")
            ErrorCode.INVALID_USER -> postErrorSnackBar("이메일 또는 비밀번호를 확인해주세요.")
            ErrorCode.NICK_NAME_NOT_FOUND -> _navigator.postValue(NavigatorConst.Screen.UPDATE_NICKNAME)
            else ->
            {
                exception.printStackTrace()
                postErrorSnackBar("[불명]${exception.message}")
            }
        }
    }

    override fun handlerInternalServerException(exception: InternalServerException)
    {
        super.handlerInternalServerException(exception)

        postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
    }

    override fun handlerException(exception: Exception)
    {
        super.handlerException(exception)
        postErrorSnackBar("[불명] ${exception.toString()}")
    }
}
