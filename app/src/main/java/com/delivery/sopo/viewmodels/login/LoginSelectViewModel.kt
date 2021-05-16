package com.delivery.sopo.viewmodels.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.networks.dto.joins.JoinInfoDTO
import com.delivery.sopo.networks.repository.JoinRepository
import com.delivery.sopo.data.repository.remote.o_auth.OAuthRemoteRepository
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.models.mapper.OAuthMapper
import com.delivery.sopo.util.SopoLog
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import kotlinx.coroutines.*
import java.util.*
import com.kakao.network.ErrorResult as KakaoErrorResult

class LoginSelectViewModel(private val userLocalRepo: UserLocalRepository, private val oAuthRepo: OAuthLocalRepository): ViewModel()
{
    val navigator = MutableLiveData<String>()
    val backgroundImage = MutableLiveData<Int>().apply { postValue(R.drawable.ic_login_ani_box) }

    /**
     * 유효성 및 통신 등의 결과 객체
     */
    private var _result = MutableLiveData<ResponseResult<*>>()
    val result: LiveData<ResponseResult<*>>
        get() = _result

    val isProgress = MutableLiveData<Boolean?>()

    /**
     * Progress turn on/off 비동기 value
     */
    fun postProgressValue(value: Boolean?) = isProgress.postValue(value)

    /**
     * Button 이벤트
     * 1. onLoginClicked() -> LoginView
     * 2. onSignUpClicked() -> SignUpView
     * 3. onKakaoLoginClicked() -> kakao login action
     */
    fun onLoginClicked()
    {
        navigator.value = NavigatorConst.TO_LOGIN
    }

    fun onSignUpClicked()
    {
        navigator.value = NavigatorConst.TO_SIGN_UP
    }

    fun onKakaoLoginClicked()
    {
        navigator.value = NavigatorConst.TO_KAKAO_LOGIN
    }

    // 카카오톡 로그인을 통해 사용자에 대한 정보를 가져온다
    fun requestKakaoLogin()
    {
        SopoLog.d(msg = "requestKakaoLogin Call()")

        val keys: MutableList<String> = ArrayList()
        keys.add("kakao_account.email")
        keys.add("properties.nickname")

        UserManagement.getInstance().me(keys, object: MeV2ResponseCallback()
        {
            override fun onFailure(errorResult: KakaoErrorResult)
            {
                super.onFailure(errorResult)
                postProgressValue(false)
                SopoLog.e(msg = "onFailure message : " + errorResult.errorMessage, e = errorResult.exception)

                // TODO KAKAO Error Response Code 필요
                _result.postValue(ResponseResult(false, null, Unit, errorResult.errorMessage))
            }

            override fun onFailureForUiThread(errorResult: KakaoErrorResult)
            {
                super.onFailureForUiThread(errorResult)
                postProgressValue(false)
                SopoLog.e(msg = "onFailureForUiThread message : " + errorResult.errorMessage, e = null)
                _result.postValue(ResponseResult(false, null, Unit, errorResult.errorMessage))
            }

            override fun onSessionClosed(errorResult: KakaoErrorResult)
            {
                SopoLog.e(msg = "onSessionClosed message : " + errorResult.errorMessage, e = errorResult.exception)

                postProgressValue(false)
                _result.postValue(ResponseResult(false, null, Unit, errorResult.errorMessage))
            }

            override fun onSuccess(result: MeV2Response)
            {
                val email = result.kakaoAccount.email
                val kakaoUserId = result.id.toString()
                val kakaoNickname = result.nickname

                SopoLog.d(msg = "onSuccess account = $email")
                SopoLog.d(msg = "onSuccess uid = $kakaoUserId")
                SopoLog.d(msg = "onSuccess nickname = $kakaoNickname")

                val password = kakaoUserId.toMD5()

                val joinInfoDTO = JoinInfoDTO(email = email, password = password, deviceInfo = SOPOApp.deviceInfo, kakaoUid = kakaoUserId, nickname = kakaoNickname)

                CoroutineScope(Dispatchers.Main).launch {
                    val res = JoinRepository.requestJoinByKakao(joinInfoDTO)

                    if (!res.result)
                    {
                        SopoLog.e("카카오 회원가입 실패")
                        _result.postValue(res)
                        return@launch
                    }

                    login(email, password, kakaoNickname)
                }
            }
        })
    }

    suspend fun login(email: String, password: String, nickname: String?)
    {
        val oAuthRes = OAuthRemoteRepository.requestLoginWithOAuth(email, password)

        if (!oAuthRes.result)
        {
            return _result.postValue(oAuthRes)
        }

        if (oAuthRes.data == null)
        {
            return _result.postValue(ResponseResult(false, null, null, "로그인 실패, 다시 시도해주세요.", DisplayEnum.DIALOG))
        }

        userLocalRepo.run {
            setUserId(email)
            setUserPassword(password)
            setStatus(1)
        }

        val oAuthEntity = OAuthMapper.objectToEntity(oAuthRes.data)

        withContext(Dispatchers.Default) { oAuthRepo.insert(oAuthEntity) }

        SOPOApp.oAuth = oAuthRes.data

        val infoRes = OAuthRemoteRepository.getUserInfo()

        if (!infoRes.result)
        {
            return _result.postValue(infoRes)
        }

        SopoLog.d("infoRes is true ${infoRes.result}")

        if (infoRes.data?.nickname == null || infoRes.data.nickname == "")
        {
            return navigator.postValue(NavigatorConst.TO_UPDATE_NICKNAME)
        }

        userLocalRepo.setNickname(infoRes.data.nickname!!)

        navigator.postValue(NavigatorConst.TO_MAIN)
    }

}