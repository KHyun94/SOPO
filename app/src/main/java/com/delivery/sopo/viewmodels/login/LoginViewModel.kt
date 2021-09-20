package com.delivery.sopo.viewmodels.login

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.exceptions.ValidateException
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.dto.OAuthDTO
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*
import java.lang.Runnable

class LoginViewModel(private val userRemoteRepo: UserRemoteRepository): ViewModel()
{
    val email = MutableLiveData<String>()
    var password = MutableLiveData<String>()

    private var _isProgress = MutableLiveData<Boolean>()
    val isProgress: LiveData<Boolean>
        get() = _isProgress

    val validity = mutableMapOf<InfoEnum, Boolean>()

    private var _invalidity = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val invalidity: LiveData<Pair<InfoEnum, Boolean>>
        get() = _invalidity

    private var _errorCode = MutableLiveData<ResponseCode>()
    val errorCode: LiveData<ResponseCode>
        get() = _errorCode

    private var _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: MutableLiveData<Triple<View, Boolean, InfoEnum>>
        get() = _focus

    val focusChangeCallback: FocusChangeCallback = FocusChangeCallback@{ v, hasFocus, type ->
        Handler(Looper.getMainLooper()).postDelayed(Runnable { _focus.value = (Triple(v, hasFocus, type)) }, 50)
    }

    init
    {
        validity[InfoEnum.EMAIL] = false
        validity[InfoEnum.PASSWORD] = false
    }

    fun onLoginClicked(v: View)
    {
        v.requestFocusFromTouch()

        _isProgress.postValue(true)

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
                CoroutineScope(Dispatchers.IO).launch {
                    requestLoginBySelf()
                    _isProgress.postValue(false)
                }
            }, 50)
    }

    private fun checkValidity()
    {
        // 입력값의 유효 처리 여부 확인
        validity.forEach { (k, v) ->
            // 유효성 체크 실패 시
            if(!v)
            {
                SopoLog.d("Type ${k.NAME}의 유효성 검사 실패 [data:${v}]")
                throw ValidateException("${k.NAME}의 유효성 검사 실패", Pair(k, v))
            }
        }
    }

    private suspend fun requestLoginBySelf()
    {
        try
        {
            SopoLog.i(msg = "requestLoginBySelf(...) 호출")

            checkValidity()

            requestLogin(email = email.value.toString(), password = password.value.toString())

            val userInfo = getUserInfo()

            if(userInfo.nickname == null)
            {
                return _navigator.postValue(NavigatorConst.TO_UPDATE_NICKNAME)
            }

            return _navigator.postValue(NavigatorConst.TO_MAIN)
        }
        catch(e: Exception)
        {
            when(e)
            {
                is ValidateException ->
                {
                    _invalidity.postValue(e.getData())
                }
                is APIException ->
                {
                    val code = e.responseCode
                    _errorCode.postValue(code)
                }
            }
        }

    }

    fun onResetPasswordClicked()
    {
        _navigator.postValue(NavigatorConst.TO_RESET_PASSWORD)
    }

    suspend fun requestLogin(email: String, password: String): OAuthDTO =
        withContext(Dispatchers.IO) {
            val loginRes = userRemoteRepo.requestLogin(email = email, password = password)
            return@withContext loginRes.data
        }

    suspend fun getUserInfo(): UserDetail = withContext(Dispatchers.IO) {
        val infoRes = userRemoteRepo.getUserInfo()
        return@withContext infoRes.data
    }
}