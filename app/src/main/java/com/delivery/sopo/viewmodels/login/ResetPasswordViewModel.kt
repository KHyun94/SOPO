package com.delivery.sopo.viewmodels.login

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.models.user.ResetPassword
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*

class ResetPasswordViewModel(private val userRemoteRepo: UserRemoteRepository): BaseViewModel()
{
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    val resetType = MutableLiveData<Int>()
    // 0: 이메일 전송 1: 패스워드 입력 2: 완료

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

    var authToken: String = ""

    var jwtToken: String = ""

    init
    {
        validity[InfoEnum.EMAIL] = false
//        validity[InfoEnum.PASSWORD] = true

//        resetType.value = 0
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
                //                        val passwordResetDTO = ResetPassword(token, email.value.toString(), password.value.toString())

                //                        val res = userRemoteRepo.requestResetPassword(resetPassword = passwordResetDTO)

                resetType.postValue(2)
                //                        _result.postValue(res)
            }
            2 ->
            {
                _navigator.postValue(NavigatorConst.TO_COMPLETE)
            }
            else ->
            {
                requestSendTokenToEmail(email = email.value?.toString() ?: "")

            }
        }
    }

    private fun requestSendTokenToEmail(email: String) = scope.launch(Dispatchers.IO) {
        SopoLog.i("requestEmailForAuth(...) 호출")
        try
        {
            jwtToken = userRemoteRepo.requestSendTokenToEmail(email = email)
            resetType.postValue(0)
            stopLoading()
        }
        catch(e: Exception)
        {
            SopoLog.e("에러 ", e)
            exceptionHandler.handleException(coroutineContext, e)
        }

    }

    private suspend fun requestPasswordForReset(email: String) = withContext(Dispatchers.IO) {
        try
        {
            val token = userRemoteRepo.requestSendTokenToEmail(email = email)
            SopoLog.d("Email Auth Info [data:${token}]")
            return@withContext token
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }
    }

    override val exceptionHandler: CoroutineExceptionHandler
        get() = CoroutineExceptionHandler { coroutineContext, throwable -> }


}