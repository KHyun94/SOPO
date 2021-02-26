package com.delivery.sopo.viewmodels.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.SuccessResult
import com.delivery.sopo.models.Result
import com.delivery.sopo.networks.handler.LoginHandler
import com.delivery.sopo.util.SopoLog
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import java.util.*

import com.kakao.network.ErrorResult as KakaoErrorResult

class LoginSelectViewModel : ViewModel()
{
    private val TAG = "LoginSelectVm"

    val loginType = MutableLiveData<String>()
    val backgroundImage = MutableLiveData<Int>().apply { postValue(R.drawable.ic_login_ani_box) }

    /**
     * 유효성 및 통신 등의 결과 객체
     */
    private var _result = MutableLiveData<Result<*, *>?>()
    val result: LiveData<Result<*, *>?>
        get() = _result

    val isProgress = MutableLiveData<Boolean?>()

    /**
     * 처리에 대한 결과 세팅
     */
    fun <T, E> postResultValue(
        successResult: SuccessResult<T>? = null,
        errorResult: ErrorResult<E>? = null
    ) = _result.postValue(Result(successResult, errorResult))

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
    fun onLoginClicked() { loginType.value = NavigatorConst.LOGIN }
    fun onSignUpClicked() { loginType.value = NavigatorConst.SIGN_UP }
    fun onKakaoLoginClicked() { loginType.value = NavigatorConst.KAKAO_LOGIN }

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

                SopoLog.e( msg = "onFailure message : " + errorResult.errorMessage, e = errorResult.exception)

                postResultValue<Unit, KakaoErrorResult>(
                    null, ErrorResult(
                        errorMsg = errorResult.errorMessage,
                        errorType = ErrorResult.ERROR_TYPE_DIALOG,
                        data = errorResult,
                        e = errorResult.exception
                    )
                )
            }

            override fun onFailureForUiThread(errorResult: KakaoErrorResult)
            {
                super.onFailureForUiThread(errorResult)
                postProgressValue(false)
                SopoLog.e(
                    msg = "onFailureForUiThread message : " + errorResult.errorMessage,
                    e = null
                )

                postResultValue<Unit, KakaoErrorResult>(
                    errorResult = ErrorResult(
                        errorMsg = errorResult.errorMessage,
                        errorType = ErrorResult.ERROR_TYPE_DIALOG,
                        data = errorResult,
                        e = errorResult.exception
                    )
                )

            }

            override fun onSessionClosed(errorResult: KakaoErrorResult)
            {
                SopoLog.e( msg = "onSessionClosed message : " + errorResult.errorMessage, e = errorResult.exception)

                postProgressValue(false)
                postResultValue<Unit, KakaoErrorResult>(
                    errorResult = ErrorResult(
                        errorMsg = errorResult.errorMessage,
                        errorType = ErrorResult.ERROR_TYPE_DIALOG, data = errorResult,
                        e = errorResult.exception
                    )
                )
            }

            override fun onSuccess(result: MeV2Response)
            {
                val email = result.kakaoAccount.email
                val kakaoUserId = result.id.toString()
                val kakaoNickname = result.nickname

                SopoLog.d(msg = "onSuccess account = $email")
                SopoLog.d(msg = "onSuccess uid = $kakaoUserId")
                SopoLog.d(msg = "onSuccess nickname = $kakaoNickname")

                LoginHandler.requestLoginByKakao(email, kakaoUserId){ successResult, errorResult ->
                    postResultValue(successResult, errorResult)
                    postProgressValue(false)
                }
            }
        })
    }

}