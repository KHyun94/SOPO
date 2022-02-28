package com.delivery.sopo.viewmodels.login

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.LockStatusConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.exceptions.UserExceptionHandler
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.user.ResetPassword
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*

class ResetPasswordViewModel(private val userRemoteRepo: UserRemoteRepository): BaseViewModel()
{
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    val resetType = MutableLiveData<Int>() // 0: 이메일 전송 1: 패스워드 입력 2: 완료

    val validity = mutableMapOf<InfoEnum, Boolean>()

    private var _invalidity = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val invalidity: LiveData<Pair<InfoEnum, Boolean>>
        get() = _invalidity

    private val _navigator = MutableLiveData<String>()
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
        override fun onFailure(error: ErrorEnum)
        {
            when(error)
            {
                ErrorEnum.INVALID_USER ->
                {
                    _invalidity.postValue(Pair(InfoEnum.EMAIL, false))
                    postErrorSnackBar(error.message)
                }
                else ->
                {
                    postErrorSnackBar(error.message)
                }
            }
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

    var jwtToken: String = ""
    var authToken: String = ""

    init
    {
        validity[InfoEnum.EMAIL] = false
    }

    fun onClearClicked()
    {
        _navigator.postValue(NavigatorConst.TO_BACK_SCREEN)
    }

    fun onSendEmailClicked(v: View) = checkEventStatus(checkNetwork = true) {
        SopoLog.i("onSendEmailClicked() 호출")

        startLoading()

        validity.forEach { (k, v) ->
            if(!v)
            {
                stopLoading()
                return@checkEventStatus _invalidity.postValue(Pair(k, v))
            }
        }

        when(resetType.value)
        {
            1 ->
            {
                val resetPassword = ResetPassword(jwtToken, authToken, email.value.toString(), password.value.toString())
                requestResetPassword(resetPassword = resetPassword)
                stopLoading()
            }
            2 ->
            {
                _navigator.postValue(NavigatorConst.TO_COMPLETE)
                stopLoading()
            }
            else ->
            {
                val email = email.value?.toString()

                if(email == null)
                {
                    postErrorSnackBar("이메일을 다시 입력해주세요.")
                    _invalidity.postValue(Pair(InfoEnum.EMAIL, false))
                    stopLoading()
                    return@checkEventStatus
                }

                requestSendTokenToEmail(email = email)
                stopLoading()
            }
        }
    }

    private fun requestSendTokenToEmail(email: String) = scope.launch(Dispatchers.IO) {
        try
        {
            jwtToken = userRemoteRepo.requestSendTokenToEmail(email = email)
            resetType.postValue(0)
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }
    }

    private fun requestResetPassword(resetPassword: ResetPassword) = scope.launch (Dispatchers.IO) {
        try
        {
            userRemoteRepo.requestResetPassword(resetPassword = resetPassword)
            resetType.postValue(2)
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }
    }




}