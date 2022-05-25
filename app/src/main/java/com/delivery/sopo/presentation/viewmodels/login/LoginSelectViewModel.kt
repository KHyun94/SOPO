package com.delivery.sopo.presentation.viewmodels.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.UserTypeConst
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.data.models.JoinInfo
import com.delivery.sopo.domain.usecase.user.token.LoginUseCase
import com.delivery.sopo.domain.usecase.user.token.SignUpUseCase
import com.delivery.sopo.util.SopoLog
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import kotlinx.coroutines.launch
import com.kakao.network.ErrorResult as KakaoErrorResult

class LoginSelectViewModel(private val loginUseCase: LoginUseCase, private val signUpUseCase: SignUpUseCase): BaseViewModel()
{
    private var email = ""
    private var kakaoUserId = ""
    private var kakaoNickname = ""

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String> get() = _navigator

    fun setNavigator(navigator: String){
        _navigator.value = navigator
    }

    fun postNavigator(navigator: String){
        _navigator.postValue(navigator)
    }

    fun onLoginClicked()
    {
        setNavigator(NavigatorConst.TO_LOGIN)
    }

    fun onSignUpClicked()
    {
        setNavigator(NavigatorConst.TO_SIGN_UP)
    }

    fun onKakaoLoginClicked() = checkEventStatus(checkNetwork = true) {
        setNavigator(NavigatorConst.TO_KAKAO_LOGIN)
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
                postErrorSnackBar("카카오 로그인에 실패했습니다. 다시 시도해주세요.")
            }

            override fun onSessionClosed(errorResult: KakaoErrorResult)
            {
                SopoLog.e(msg = "onSessionClosed message : " + errorResult.errorMessage, e = errorResult.exception)
                postErrorSnackBar("세션 종료로 인해, 카카오 로그인에 실패했습니다.")
            }

            override fun onFailure(errorResult: KakaoErrorResult)
            {
                super.onFailure(errorResult)
                SopoLog.e(msg = "onFailure message : " + errorResult.errorMessage, e = errorResult.exception)
                postErrorSnackBar("카카오 로그인에 실패했습니다.")
            }

            override fun onSuccess(result: MeV2Response)
            {
                email = result.kakaoAccount.email
                kakaoUserId = result.id.toString()
                kakaoNickname = result.nickname

                scope.launch(coroutineExceptionHandler) {
                    onStartLoading()
                    SopoLog.d("로딩")
                    requestJoinByKakao(email = email, uId = kakaoUserId, nickname = kakaoNickname)
                    requestLoginByKakao(email = email, uId = kakaoUserId)
                    SopoLog.d("스탑")
                    onStopLoading()
                }
            }
        })
    }

    private suspend fun requestJoinByKakao(email: String, uId: String, nickname: String)
    {
        SopoLog.i(msg = "requestJoinByKakao(...) 호출")
        val joinInfo = JoinInfo(email = email, password = uId.toMD5(), kakaoUid = uId, nickname = nickname)
        signUpUseCase.invoke(joinInfo = joinInfo, userType = UserTypeConst.KAKAO)
    }

    private suspend fun requestLoginByKakao(email: String, uId: String)
    {
        SopoLog.i(msg = "requestLoginBySelf(...) 호출")
        loginUseCase.invoke(username = email, password = uId.toMD5())
        return postNavigator(NavigatorConst.TO_MAIN)
    }

    override var onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onAlreadyRegisteredUser(error: ErrorCode)
        {
            super.onAlreadyRegisteredUser(error)

            scope.launch(coroutineExceptionHandler) {
                requestLoginByKakao(email = email, uId = kakaoUserId)
            }
        }

        override fun onLoginError(error: ErrorCode)
        {
            super.onLoginError(error)

            postErrorSnackBar("유효한 이메일 또는 비밀번호가 아닙니다.")
        }

        override fun onInternalServerError(error: ErrorCode)
        {
            super.onInternalServerError(error)
            postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.[${error.toString()}]")
        }

        override fun onAuthError(error: ErrorCode)
        {
            super.onAuthError(error)
            postErrorSnackBar("로그인이 실패했습니다. 다시 시도해주세요.[${error.toString()}]")
        }

        override fun onFailure(error: ErrorCode)
        {
            when(error)
            {
                ErrorCode.NICK_NAME_NOT_FOUND ->
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

}