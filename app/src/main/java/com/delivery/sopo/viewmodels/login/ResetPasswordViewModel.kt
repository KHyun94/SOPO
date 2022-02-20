package com.delivery.sopo.viewmodels.login

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.models.EmailAuthDTO
import com.delivery.sopo.models.PasswordResetDTO
import com.delivery.sopo.models.ResponseResult
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
        SopoLog.i("${type.NAME} >>> $hasFocus")
        _focus.value = (Triple(v, hasFocus, type))
    }

    // 유효성 및 통신 등의 결과 객체
    private var _result = MutableLiveData<ResponseResult<*>>()
    val result: LiveData<ResponseResult<*>>
        get() = _result

    lateinit var token: String

    var jwtTokenForReset: String? = null

    init
    {
//        validity[InfoEnum.EMAIL] = false
//        validity[InfoEnum.PASSWORD] = true

        resetType.value = 0
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

        CoroutineScope(Dispatchers.IO).launch {

            try
            {
                when(resetType.value)
                {
                    0 ->
                    {
                        token = requestEmailForAuth(email = email.value?.toString() ?: "")
                        SopoLog.d("Auth JWT Token [data:${token.toString()}]")
                        resetType.postValue(1)
                        stopLoading()
                    }
                    1 ->
                    {
                        val passwordResetDTO = PasswordResetDTO(token, email.value.toString(), password.value.toString())

                        val res = userRemoteRepo.requestPasswordForReset(passwordResetDTO = passwordResetDTO)

                        resetType.postValue(2)
//                        _result.postValue(res)
                    }
                    2 ->
                    {
                        _navigator.postValue(NavigatorConst.TO_COMPLETE)
                    }
                }
            }
            catch(e: Exception)
            {
                SopoLog.e("에러 ", e)
                exceptionHandler.handleException(coroutineContext, e)
            }


        }
    }

    private suspend fun requestEmailForAuth(email: String): String
    {
        SopoLog.i("requestEmailForAuth(...) 호출")
        return userRemoteRepo.requestEmailForAuth(email = email)
    }

    private suspend fun requestPasswordForReset(email: String) = withContext(Dispatchers.IO) {
        try
        {
            val token = userRemoteRepo.requestEmailForAuth(email = email)
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