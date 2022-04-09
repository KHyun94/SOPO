package com.delivery.sopo.viewmodels.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.exceptions.UserExceptionHandler
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.local.repository.CarrierRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.networks.dto.joins.JoinInfo
import com.delivery.sopo.networks.repository.JoinRepositoryImpl
import com.delivery.sopo.util.SopoLog
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import kotlinx.coroutines.*
import java.util.*
import com.kakao.network.ErrorResult as KakaoErrorResult

class LoginSelectViewModel(private val userRemoteRepo: UserRemoteRepository, private val carrierRepo: CarrierRepository, private val joinRepoImpl: JoinRepositoryImpl):
        BaseViewModel()
{
    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    private var email = ""
    private var kakaoUserId = ""
    private var kakaoNickname = ""

    private val onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onErrorAlreadyRegisteredUser(error: ErrorEnum)
        {
            super.onErrorAlreadyRegisteredUser(error)

            if(error == ErrorEnum.ALREADY_REGISTERED_USER)
            {
                requestLoginByKakao(email = email, uId = kakaoUserId)
                return
            }
        }

        override fun onLoginError(error: ErrorEnum)
        {
            super.onLoginError(error)

            postErrorSnackBar("유효한 이메일 또는 비밀번호가 아닙니다.")
        }

        override fun onInternalServerError(error: ErrorEnum)
        {
            super.onInternalServerError(error)
            postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.[${error.toString()}]")
        }

        override fun onAuthError(error: ErrorEnum)
        {
            super.onAuthError(error)
            postErrorSnackBar("로그인이 실패했습니다. 다시 시도해주세요.[${error.toString()}]")
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
    }

    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        UserExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
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

    fun onKakaoLoginClicked() = checkEventStatus(checkNetwork = true, delayMillisecond = 0) {
        _navigator.value = NavigatorConst.TO_KAKAO_LOGIN
    }

    // 카카오톡 로그인을 통해 사용자에 대한 정보를 가져온다
    fun requestKakaoLogin()
    {
        SopoLog.i(msg = "requestKakaoLogin(...) 호출")

        onStartLoading()

        val keys: MutableList<String> = ArrayList()
        keys.add("kakao_account.email")
        keys.add("properties.nickname")

        UserManagement.getInstance().me(keys, object: MeV2ResponseCallback()
        {
            override fun onFailureForUiThread(errorResult: KakaoErrorResult)
            {
                super.onFailureForUiThread(errorResult)
                onStopLoading()
                SopoLog.e(msg = "onFailureForUiThread message : " + errorResult.errorMessage, e = null)
                postErrorSnackBar("카카오 로그인에 실패했습니다. 다시 시도해주세요.")
            }

            override fun onSessionClosed(errorResult: KakaoErrorResult)
            {
                onStopLoading()
                SopoLog.e(msg = "onSessionClosed message : " + errorResult.errorMessage, e = errorResult.exception)
                postErrorSnackBar("세션 종료로 인해, 카카오 로그인에 실패했습니다.")
            }

            override fun onFailure(errorResult: KakaoErrorResult)
            {
                super.onFailure(errorResult)
                onStopLoading()
                SopoLog.e(msg = "onFailure message : " + errorResult.errorMessage, e = errorResult.exception)
                postErrorSnackBar("카카오 로그인에 실패했습니다.")
            }

            override fun onSuccess(result: MeV2Response)
            {
                email = result.kakaoAccount.email
                kakaoUserId = result.id.toString()
                kakaoNickname = result.nickname

                scope.launch(Dispatchers.IO) {

                    onStartLoading()

                    val isSuccess =
                        requestJoinByKakao(email = email, uId = kakaoUserId, nickname = kakaoNickname)

                    if(!isSuccess)
                    {

                        return@launch
                    }

                    requestLoginByKakao(email = email, uId = kakaoUserId)
                }

            }
        })
    }

    private suspend fun requestJoinByKakao(email: String, uId: String, nickname: String): Boolean =
        withContext(Dispatchers.IO) {
            try
            {
                SopoLog.i(msg = "requestJoinByKakao(...) 호출")

                val joinInfo =
                    JoinInfo(email = email, password = uId.toMD5(), kakaoUid = uId, nickname = nickname)
                joinRepoImpl.requestJoinByKakao(joinInfo = joinInfo)
                return@withContext true
            }
            catch(e: Exception)
            {
                onStopLoading()
                exceptionHandler.handleException(coroutineContext, e)
                return@withContext false
            }
        }

    private fun requestLoginByKakao(email: String, uId: String) = scope.launch(Dispatchers.IO) {
            try
            {
                SopoLog.i(msg = "requestLoginBySelf(...) 호출")

                userRemoteRepo.requestLogin(email, uId.toMD5())

                userRemoteRepo.getUserInfo()

                return@launch _navigator.postValue(NavigatorConst.TO_MAIN)
            }
            catch(e: Exception)
            {
                exceptionHandler.handleException(coroutineContext, e)
            }
            finally
            {
                onStopLoading()
            }
        }
}