package com.delivery.sopo.viewmodels.login

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.UserExceptionHandler
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.exceptions.ValidateException
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.dto.OAuthDTO
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*
import java.lang.Runnable

class LoginViewModel(private val userRemoteRepo: UserRemoteRepository): BaseViewModel()
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

    private val onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onLoginError(error: ErrorEnum)
        {
            super.onLoginError(error)

            postErrorSnackBar("유효한 이메일 또는 비밀번호가 아닙니다.")
        }

        override fun onFailure(error: ErrorEnum)
        {
            when(error)
            {
                ErrorEnum.NICK_NAME_NOT_FOUND ->
                {
                    _navigator.postValue(NavigatorConst.TO_UPDATE_NICKNAME)
                }
                else ->
                {
                    postErrorSnackBar("로그인이 실패했습니다. 다시 시도해주세요.[${error.toString()}]")
                }
            }
        }

        override fun onInternalServerError(error: ErrorEnum)
        {
            super.onInternalServerError(error)

            postErrorSnackBar("일시적으로 서비스를 이용할 수 없습니다.")
        }

        override fun onAuthError(error: ErrorEnum)
        {
            super.onAuthError(error)

            postErrorSnackBar("로그인이 실패했습니다. 다시 시도해주세요.[${error.toString()}]")
        }
    }

    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        UserExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }

    init
    {
        validity[InfoEnum.EMAIL] = false
        validity[InfoEnum.PASSWORD] = false
    }

    fun onLoginClicked(v: View)
    {
        v.requestFocusFromTouch()
        requestLoginBySelf()
    }

    private fun requestLoginBySelf() = scope.launch(Dispatchers.IO) {
        try
        {
            SopoLog.i(msg = "requestLoginBySelf(...) 호출")

            // 입력값의 유효 처리 여부 확인
            validity.forEach { (k, v) ->
                if(!v)
                {
                    return@launch _invalidity.postValue(Pair(k, v))
                }
            }

            userRemoteRepo.requestLogin(email = email.value.toString(), password = password.value.toString().toMD5())
            userRemoteRepo.getUserInfo()

            return@launch _navigator.postValue(NavigatorConst.TO_MAIN)
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }

    }

    fun onResetPasswordClicked()
    {
        _navigator.postValue(NavigatorConst.TO_RESET_PASSWORD)
    }
}