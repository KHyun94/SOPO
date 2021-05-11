package com.delivery.sopo.viewmodels.login

import android.os.Handler
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.data.repository.remote.o_auth.OAuthRemoteRepository
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(val userLocalRepository: UserLocalRepository, val oAuthRepo: OAuthLocalRepository): ViewModel()
{
    val email = MutableLiveData<String>()
    var password = MutableLiveData<String>()

    val validities = mutableMapOf<InfoEnum, Boolean>()

    private var _invalidity = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val invalidity: LiveData<Pair<InfoEnum, Boolean>>
        get() = _invalidity

    // 유효성 및 통신 등의 결과 객체
    private var _result = MutableLiveData<ResponseResult<*>>()
    val result: LiveData<ResponseResult<*>>
        get() = _result

    private var _isProgress = MutableLiveData<Boolean>()
    val isProgress: LiveData<Boolean>
        get() = _isProgress

    private var _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: MutableLiveData<Triple<View, Boolean, InfoEnum>>
        get() = _focus

    val focusChangeCallback: FocusChangeCallback = FocusChangeCallback@{ v, hasFocus, type ->
        SopoLog.i("${type.NAME} >>> $hasFocus")
        Handler().postDelayed(Runnable { _focus.value = (Triple(v, hasFocus, type)) }, 50)
    }

    init
    {
        validities[InfoEnum.EMAIL] = false
        validities[InfoEnum.PASSWORD] = false
    }

    fun onLoginClicked(v: View)
    {
        SopoLog.d(msg = "onLoginClicked() call!!!")

        try
        {
            // 입력값의 유효 처리 여부 확인
            validities.forEach { (k, v) ->

                // 유효성 체크 실패 시
                if (!v)
                {
                    SopoLog.d("${k.NAME}'s validity is fail")
                    _invalidity.postValue(Pair(k, v))
                    return@onLoginClicked
                }
            }

            // 성공
            CoroutineScope(Dispatchers.IO).launch {
                OAuthRemoteRepository.requestLoginWithOAuth(email.value.toString(), password.value.toString().toMD5())
            }
        }
        catch (e: Exception)
        {
            throw e
        }
    }

    fun onResetPasswordClicked()
    {
        _navigator.postValue(NavigatorConst.TO_RESET_PASSWORD)
    }

//    suspend fun loginWithOAuth(email: String, password: String)
//    {
//        when (val result = LoginAPICall().requestOauth(email, password, SOPOApp.deviceInfo))
//        {
//            is NetworkResult.Success ->
//            {
//                userLocalRepository.setUserId(email)
//                userLocalRepository.setUserPassword(password)
//                userLocalRepository.setStatus(1)
//
//                val oAuth = Gson().let { gson ->
//                    val type = object: TypeToken<OauthResult>()
//                    {}.type
//                    val reader = gson.toJson(result.data)
//                    val data = gson.fromJson<OauthResult>(reader, type)
//                    OauthMapper.objectToEntity(data)
//                }
//
//                SOPOApp.oAuth = oAuth
//
//                withContext(Dispatchers.Default) {
//                    oAuthRepo.insert(oAuth)
//                }
//
//                _isProgress.postValue(false)
//
//                val userInfoRes = OAuthNetworkRepo.getUserInfo()
//
//                _result.postValue(userInfoRes)
//            }
//            is NetworkResult.Error ->
//            {
//                val exception = result.exception as APIException
//                val code = exception.responseCode
//                _isProgress.postValue(false)
//                _result.postValue(ResponseResult(false, code, Unit, exception.errorMessage, DisplayEnum.DIALOG))
//            }
//        }
//    }
}