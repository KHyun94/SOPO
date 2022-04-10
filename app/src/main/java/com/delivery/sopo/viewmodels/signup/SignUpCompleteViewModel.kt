package com.delivery.sopo.viewmodels.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.exceptions.UserExceptionHandler
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUpCompleteViewModel(private val userLocalRepo: UserLocalRepository, private val userRemoteRepo: UserRemoteRepository):
        BaseViewModel()
{
    val email = MutableLiveData<String>().also {
        it.postValue(userLocalRepo.getUserId())
    }

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    fun postNavigator(navigator: String){ _navigator.postValue(navigator) }

    override var onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onFailure(error: ErrorEnum)
        {
            when(error)
            {
                ErrorEnum.NICK_NAME_NOT_FOUND ->
                {
                    _navigator.postValue(NavigatorConst.TO_UPDATE_NICKNAME)
                }
                ErrorEnum.USER_NOT_FOUND ->
                {
                    postErrorSnackBar("로그인이 실패했습니다. 다시 시도해주세요.")
                    _navigator.postValue(NavigatorConst.TO_LOGIN)
                }
                else ->
                {
                    postErrorSnackBar("로그인이 실패했습니다. 다시 시도해주세요.")
                }
            }
        }

        override fun onLoginError(error: ErrorEnum)
        {
            super.onLoginError(error)
            postErrorSnackBar("유효한 이메일 또는 비밀번호가 아닙니다.")
        }

        override fun onAuthError(error: ErrorEnum)
        {
            super.onAuthError(error)
            postErrorSnackBar("인증에 실패했습니다.")
        }

        override fun onInternalServerError(error: ErrorEnum)
        {
            super.onInternalServerError(error)
            postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
        }
    }

    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        UserExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }

    fun onCompleteClicked()
    {
        requestLogin(userLocalRepo.getUserId(), userLocalRepo.getUserPassword())
    }

    private fun requestLogin(email: String, password: String) = scope.launch(Dispatchers.IO) {
        try
        {
            onStartLoading()
            userRemoteRepo.requestLogin(email, password)
            userRemoteRepo.getUserInfo()
            postNavigator(NavigatorConst.TO_MAIN)
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(context = coroutineContext, exception = e)
        }
        finally
        {
            onStopLoading()
        }

    }
}
