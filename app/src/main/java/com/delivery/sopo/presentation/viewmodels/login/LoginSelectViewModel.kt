package com.delivery.sopo.presentation.viewmodels.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.consts.UserTypeConst
import com.delivery.sopo.data.models.JoinInfo
import com.delivery.sopo.domain.usecase.user.token.LoginUseCase
import com.delivery.sopo.domain.usecase.user.token.SignUpUseCase
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.thirdpartyapi.KakaoOath
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.launch

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
    fun requestKakaoLogin(kakaoOath: KakaoOath)
    {
        SopoLog.i(msg = "requestKakaoLogin(...) 호출")

        kakaoOath.signIn(onSuccess = { user ->
            email = user.kakaoAccount?.email.toString()
            kakaoUserId = user.id.toString()
            kakaoNickname = user.kakaoAccount?.name.toString()

            scope.launch {
                onStartLoading()
                requestJoinByKakao(email = email, uId = kakaoUserId, nickname = kakaoNickname)
                requestLoginByKakao(email = email, uId = kakaoUserId)
                onStopLoading()
            }
        }, onFailure = {
            postErrorSnackBar("${it.message}")
            it.printStackTrace()
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
        return postNavigator(NavigatorConst.Screen.MAIN)
    }

    override fun handlerAPIException(exception: SOPOApiException)
    {
        super.handlerAPIException(exception)
        when(exception.code)
        {
            ErrorCode.VALIDATION -> postErrorSnackBar(exception.message)
            ErrorCode.USER_NOT_FOUND -> postErrorSnackBar("계정 정보를 찾을 수 없습니다.")
            ErrorCode.ALREADY_REGISTERED_USER -> {
                scope.launch(exceptionHandler) {
                    requestLoginByKakao(email = email, uId = kakaoUserId)
                }
            }
            ErrorCode.INVALID_USER -> postErrorSnackBar("이메일 또는 비밀번호를 확인해주세요.")
            ErrorCode.NICK_NAME_NOT_FOUND -> postNavigator(NavigatorConst.Screen.UPDATE_NICKNAME)
            else ->
            {
                exception.printStackTrace()
                postErrorSnackBar("로그인이 실패했습니다. 다시 시도해주세요.[${exception.toString()}]")
            }
        }
    }

    override fun handlerInternalServerException(exception: InternalServerException)
    {
        super.handlerInternalServerException(exception)

        postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
    }

    override fun handlerException(exception: Exception)
    {
        super.handlerException(exception)
        postErrorSnackBar("[불명] ${exception.toString()}")
    }
}