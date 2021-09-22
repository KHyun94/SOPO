package com.delivery.sopo.viewmodels.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.dto.OAuthDTO
import com.delivery.sopo.networks.dto.joins.JoinInfoDTO
import com.delivery.sopo.networks.repository.JoinRepository
import com.delivery.sopo.util.SopoLog
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import kotlinx.coroutines.*
import java.util.*
import com.kakao.network.ErrorResult as KakaoErrorResult

class LoginSelectViewModel(private val userLocalRepo: UserLocalRepository, private val userRemoteRepo:UserRemoteRepository, private val joinRepo:JoinRepository): ViewModel()
{
    private val viewModelScope: CoroutineScope

    private val _navigator = MutableLiveData<String>()
    val navigator : LiveData<String>
        get() = _navigator

    private val _isProgress = MutableLiveData<Boolean>()
    val isProgress: LiveData<Boolean>
        get() = _isProgress

    private var _errorCode = MutableLiveData<ResponseCode>()
    val errorCode: LiveData<ResponseCode>
        get() = _errorCode

    init
    {
        viewModelScope = CoroutineScope(Dispatchers.Main)
    }

    /**
     * Button 이벤트
     * 1. onLoginClicked() -> LoginView
     * 2. onSignUpClicked() -> SignUpView
     * 3. onKakaoLoginClicked() -> kakao login action
     */
    fun onLoginClicked()
    {

        _navigator.value = NavigatorConst.TO_LOGIN
    }

    fun onSignUpClicked()
    {
        _navigator.value = NavigatorConst.TO_SIGN_UP
    }

    fun onKakaoLoginClicked()
    {
        _navigator.value = NavigatorConst.TO_KAKAO_LOGIN
    }

    // 카카오톡 로그인을 통해 사용자에 대한 정보를 가져온다
    fun requestKakaoLogin()
    {
        SopoLog.i(msg = "requestKakaoLogin(...) 호출")

        val keys: MutableList<String> = ArrayList()
        keys.add("kakao_account.email")
        keys.add("properties.nickname")

        UserManagement.getInstance().me(keys, object: MeV2ResponseCallback()
        {
            override fun onFailureForUiThread(errorResult: KakaoErrorResult)
            {
                super.onFailureForUiThread(errorResult)
                SopoLog.e(msg = "onFailureForUiThread message : " + errorResult.errorMessage, e = null)
                _errorCode.postValue(ResponseCode.FAIL_TO_LOGIN_KAKAO)
            }

            override fun onSessionClosed(errorResult: KakaoErrorResult)
            {
                SopoLog.e(msg = "onSessionClosed message : " + errorResult.errorMessage, e = errorResult.exception)
                _errorCode.postValue(ResponseCode.FAIL_TO_LOGIN_KAKAO)
            }

            override fun onFailure(errorResult: KakaoErrorResult)
            {
                super.onFailure(errorResult)
                SopoLog.e(msg = "onFailure message : " + errorResult.errorMessage, e = errorResult.exception)
                _errorCode.postValue(ResponseCode.FAIL_TO_LOGIN_KAKAO)
            }

            override fun onSuccess(result: MeV2Response)
            {
                _isProgress.postValue(true)

                val email = result.kakaoAccount.email
                val kakaoUserId = result.id.toString()
                val kakaoNickname = result.nickname

                requestLoginByKakao(email = email, uId = kakaoUserId, nickname = kakaoNickname)
            }
        })
    }

    private fun requestLoginByKakao(email: String, uId:String, nickname:String)
    {
        viewModelScope.launch {
            try
            {
                SopoLog.i(msg = "requestLoginBySelf(...) 호출")

                val joinInfo = JoinInfoDTO(email = email, password = uId.toMD5(), deviceInfo = SOPOApp.deviceInfo, kakaoUid = uId, nickname = nickname)
                // 회원 가입
                requestJoinByKakao(joinInfo = joinInfo)

                requestLogin(email, uId)

                val userInfo = getUserInfo()

                if(userInfo.nickname != null)
                {
                    return@launch _navigator.postValue(NavigatorConst.TO_MAIN)
                }

                return@launch _navigator.postValue(NavigatorConst.TO_UPDATE_NICKNAME)

            }
            catch(e: Exception)
            {
                when(e)
                {
                    is APIException ->
                    {
                        val code = e.responseCode

                        if(code == ResponseCode.ALREADY_REGISTERED_USER)
                        {
                            requestLogin(email, uId)

                            val userInfo = getUserInfo()

                            if(userInfo.nickname != null)
                            {
                                return@launch _navigator.postValue(NavigatorConst.TO_MAIN)
                            }

                            return@launch _navigator.postValue(NavigatorConst.TO_UPDATE_NICKNAME)
                        }

                        _errorCode.postValue(code)
                    }
                    else ->
                    {
                        _errorCode.postValue(ResponseCode.UNKNOWN_ERROR)
                    }
                }
            }
            finally
            {
                _isProgress.postValue(false)
            }
        }
    }

    fun onResetPasswordClicked()
    {
        _navigator.postValue(NavigatorConst.TO_RESET_PASSWORD)
    }

    suspend fun requestJoinByKakao(joinInfo: JoinInfoDTO) = withContext(Dispatchers.IO) {
            val res =joinRepo.requestJoinByKakao(joinInfo)
            return@withContext
        }

    suspend fun requestLogin(email: String, password: String): OAuthDTO = withContext(Dispatchers.IO) {
            val loginRes = userRemoteRepo.requestLogin(email = email, password = password)
            return@withContext loginRes.data
        }

    suspend fun getUserInfo(): UserDetail = withContext(Dispatchers.IO) {
        val infoRes = userRemoteRepo.getUserInfo()
        return@withContext infoRes.data
    }

}