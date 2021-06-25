package com.delivery.sopo.viewmodels.login

import android.os.Handler
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.data.repository.remote.o_auth.OAuthRemoteRepository
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.mapper.OAuthMapper
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(private val userLocalRepo: UserLocalRepository, private val oAuthRepo: OAuthLocalRepository):
        ViewModel()
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
        SopoLog.d("지금이니이이")
        v.requestFocusFromTouch()
        Handler().postDelayed(Runnable {  loginEvent(v)}, 50)
    }

    fun loginEvent(v: View)
    {
        SopoLog.d(msg = "onLoginClicked() call!!!")

        try
        {
            // 입력값의 유효 처리 여부 확인
            validities.forEach { (k, v) ->
                // 유효성 체크 실패 시
                if(!v)
                {
                    SopoLog.d("${k.NAME}'s validity is fail ${password.value}")
                    _invalidity.postValue(Pair(k, v))
                    return@loginEvent
                }
            }

            // 성공
            CoroutineScope(Dispatchers.IO).launch {
                val res = login(email.value.toString(), password.value.toString().toMD5())

                if(!res.result) return@launch _result.postValue(res)

                val userDetail = res.data as UserDetail

                if(userDetail.nickname == null || userDetail.nickname == "")
                {
                    return@launch _navigator.postValue(NavigatorConst.TO_UPDATE_NICKNAME)
                }

                return@launch _navigator.postValue(NavigatorConst.TO_MAIN)
            }
        }
        catch(e: Exception)
        {
            throw e
        }
    }

    fun onResetPasswordClicked()
    {
        _navigator.postValue(NavigatorConst.TO_RESET_PASSWORD)
    }


    suspend fun login(email: String, password: String): ResponseResult<*>
    {
        val oAuthRes = OAuthRemoteRepository.requestLoginWithOAuth(email, password)

        if(!oAuthRes.result)
        {
            return oAuthRes
        }

        val oAuthDTO =
            oAuthRes.data ?: return ResponseResult(false, ResponseCode.ERROR_RESPONSE_DATA_IS_NULL,
                                                   null, "로그이 실패했습니다. 다시 시도해주세.",
                                                   DisplayEnum.DIALOG)

        userLocalRepo.run {
            setUserId(email)
            setUserPassword(password)
            setStatus(1)
        }

        SOPOApp.oAuth = oAuthDTO

        val oAuthEntity = OAuthMapper.objectToEntity(oAuth = oAuthDTO)

        withContext(Dispatchers.Default) { oAuthRepo.insert(oAuthEntity) }

        val infoRes = OAuthRemoteRepository.getUserInfo()

        if(!infoRes.result)
        {
            return infoRes
        }

        val userDetail =
            infoRes.data ?: return ResponseResult(false, ResponseCode.ERROR_RESPONSE_DATA_IS_NULL,
                                                  null, "로그인 실패했습니다. 다시 시도해주세요.",
                                                  DisplayEnum.DIALOG)

        userLocalRepo.setNickname(userDetail.nickname ?: "")

        return infoRes
    }
}