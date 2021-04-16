package com.delivery.sopo.viewmodels.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.extensions.md5
import com.delivery.sopo.mapper.OauthMapper
import com.delivery.sopo.models.OauthResult
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.networks.api.LoginAPICall
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.networks.repository.JoinRepository
import com.delivery.sopo.networks.repository.OAuthNetworkRepo
import com.delivery.sopo.repository.impl.OauthRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import com.kakao.network.ErrorResult as KakaoErrorResult

class LoginSelectViewModel(private val userRepo: UserRepoImpl, private val oAuthRepo: OauthRepoImpl) : ViewModel()
{
    val loginType = MutableLiveData<String>()
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
    fun onLoginClicked() { loginType.value = NavigatorConst.TO_LOGIN }
    fun onSignUpClicked() { loginType.value = NavigatorConst.TO_SIGN_UP }
    fun onKakaoLoginClicked() { loginType.value = NavigatorConst.TO_KAKAO_LOGIN }

    // 카카오톡 로그인을 통해 사용자에 대한 정보를 가져온다
    fun requestKakaoLogin()
    {
        SopoLog.d( msg = "requestKakaoLogin Call()")
        
        postProgressValue(true)
        
        val keys: MutableList<String> = ArrayList()
        keys.add("kakao_account.email")

        UserManagement.getInstance().me(keys, object : MeV2ResponseCallback()
        {
            override fun onFailure(errorResult: KakaoErrorResult)
            {
                super.onFailure(errorResult)
                postProgressValue(false)
                SopoLog.e( msg = "onFailure message : " + errorResult.errorMessage, e = errorResult.exception)

                // TODO KAKAO Error Response Code 필
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
                SopoLog.e( msg = "onSessionClosed message : " + errorResult.errorMessage, e = errorResult.exception)

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

                val password = kakaoUserId.md5()

                CoroutineScope(Dispatchers.Main).launch {
                    val res = JoinRepository.requestJoinByKakao(email, password, kakaoUserId, kakaoNickname)

                    postProgressValue(false)
                    _result.postValue(res)
                }
            }
        })
    }

    fun login(email: String, password: String)
    {
        CoroutineScope(Dispatchers.Main).launch {
            val oAuthRes = OAuthNetworkRepo.loginWithOAuth(email, password)

            if(!oAuthRes.result)
            {
                _result.postValue(oAuthRes)
                return@launch
            }

            userRepo.run {
                setEmail(email)
                setApiPwd(password)
                setStatus(1)
            }

            SOPOApp.oAuthEntity = oAuthRes.data

            if(userRepo.getNickname() == "")
            {
                val infoRes = OAuthNetworkRepo.getUserInfo()

                if(!infoRes.result)
                {
                    _result.postValue(infoRes)
                    return@launch
                }




            }
        }

    }

    // TODO 통합 필
    suspend fun loginWithOAuth(email: String, password: String)
    {
        when(val result = LoginAPICall().requestOauth(email, password, SOPOApp.deviceInfo))
        {
            is NetworkResult.Success ->
            {
                userRepo.setEmail(email)
                userRepo.setApiPwd(password)
                userRepo.setStatus(1)

                val oAuth = Gson().let { gson ->
                    val type = object : TypeToken<OauthResult>() {}.type
                    val reader = gson.toJson(result.data)
                    val data = gson.fromJson<OauthResult>(reader, type)
                    OauthMapper.objectToEntity(data)
                }

                SOPOApp.oAuthEntity = oAuth

                withContext(Dispatchers.Default){
                    oAuthRepo.insert(oAuth)
                }

                _result.postValue(getUserInfo())
            }
            is NetworkResult.Error ->
            {
                val exception = result.exception as APIException
                val code = exception.responseCode
                _result.postValue(ResponseResult(false, code, Unit, code.MSG, DisplayEnum.DIALOG))
            }
        }
    }

    suspend fun getUserInfo(): ResponseResult<UserDetail?>
    {
        val result = UserCall.getUserInfoWithToken()

        if(result is NetworkResult.Error)
        {
            val exception = result.exception as APIException
            val responseCode = exception.responseCode
            val date = SOPOApp.oAuthEntity.let { it?.expiresIn }

            return if(responseCode.HTTP_STATUS == 401 && DateUtil.isOverExpiredDate(date!!)) ResponseResult(false, responseCode, null, "로그인 기한이 만료되었습니다.\n다시 로그인해주세요.", DisplayEnum.DIALOG)
            else ResponseResult(false, responseCode, null, responseCode.MSG, DisplayEnum.DIALOG)
        }

        val apiResult = (result as NetworkResult.Success).data

        userRepo.setNickname(apiResult.data?.nickname?:"")

        SopoLog.d("UserDetail >>> ${apiResult.data}, ${apiResult.data?.nickname}")

        return ResponseResult(true, ResponseCode.SUCCESS, apiResult.data, ResponseCode.SUCCESS.MSG)
    }

}