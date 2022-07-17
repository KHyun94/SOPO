package com.delivery.sopo.presentation.viewmodels.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.data.resources.user.local.UserDataSource
import com.delivery.sopo.domain.usecase.user.token.LoginUseCase
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import kotlinx.coroutines.launch

class SignUpCompleteViewModel(private val loginUseCase: LoginUseCase, private val userDataSource: UserDataSource):
        BaseViewModel()
{
    val email = MutableLiveData<String>().also {
        it.postValue(userDataSource.getUsername())
    }

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    fun postNavigator(navigator: String)
    {
        _navigator.postValue(navigator)
    }

    override fun handlerAPIException(exception: SOPOApiException)
    {
        super.handlerAPIException(exception)
        when(exception.code)
        {
            ErrorCode.NICK_NAME_NOT_FOUND -> postNavigator(NavigatorConst.Screen.UPDATE_NICKNAME)
            ErrorCode.USER_NOT_FOUND ->
            {
                postErrorSnackBar("로그인이 실패했습니다. 다시 시도해주세요.")
                postNavigator(NavigatorConst.TO_LOGIN)
            }
            else ->
            {
                postErrorSnackBar("로그인이 실패했습니다. 다시 시도해주세요.")
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

    fun onCompleteClicked()
    {
        requestLogin(userDataSource.getUsername(), userDataSource.getUserPassword())
    }

    private fun requestLogin(userName: String, password: String) = scope.launch {
        try
        {
            onStartLoading()

            loginUseCase.invoke(userName, password)

            postNavigator(NavigatorConst.Screen.MAIN)
        }
        finally
        {
            onStopLoading()
        }

    }
}
