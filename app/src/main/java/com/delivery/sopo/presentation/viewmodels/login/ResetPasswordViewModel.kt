package com.delivery.sopo.presentation.viewmodels.login

import android.os.Build
import android.text.Html
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.presentation.bindings.FocusChangeCallback
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.domain.usecase.user.reset.ResetPasswordUseCase
import com.delivery.sopo.domain.usecase.user.reset.SendAuthTokenUseCase
import com.delivery.sopo.domain.usecase.user.reset.VerifyAuthTokenUseCase
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.user.ResetAuthCode
import com.delivery.sopo.models.user.ResetPassword
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.OnSnackBarClickListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
        private val sendAuthTokenUseCase: SendAuthTokenUseCase,
        private val verifyAuthTokenUseCase: VerifyAuthTokenUseCase,
        private val resetPasswordUseCase: ResetPasswordUseCase
): BaseViewModel()
{
    val username = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    val validity = mutableMapOf<InfoEnum, Boolean>()

    private val _invalidity = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val invalidity: LiveData<Pair<InfoEnum, Boolean>> = _invalidity

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String> = _navigator

    val authCode = MutableLiveData<String>()

    fun postNavigator(navigator: String)
    {
        _navigator.postValue(navigator)
    }

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: MutableLiveData<Triple<View, Boolean, InfoEnum>> = _focus

    private val _focusOn = MutableLiveData<InfoEnum>()
    val focusOn: MutableLiveData<InfoEnum> = _focusOn

    var cnfOfFailureAuthCode: Int = 0

    val focusChangeCallback: FocusChangeCallback = FocusChangeCallback@{ v, hasFocus, type ->
        _focus.value = (Triple(v, hasFocus, type))
    }

    var jwtToken: String = ""
    var authToken: String = ""

    init
    {
        validity[InfoEnum.EMAIL] = false
        postNavigator(NavigatorConst.Event.INPUT_EMAIL_FOR_SEND)
    }

    fun onClearClicked()
    {
        postNavigator(NavigatorConst.Event.BACK)
    }

    fun onSendEmailClicked(v: View) = checkEventStatus(checkNetwork = true) {
        SopoLog.i("onSendEmailClicked() 호출")

        validity.forEach { (k, v) ->
            if(!v) return@checkEventStatus _invalidity.postValue(Pair(k, v))
        }

        val email = username.value?.toString()

        if(email == null)
        {
            postErrorSnackBar("이메일을 다시 입력해주세요.")
            _invalidity.postValue(Pair(InfoEnum.EMAIL, false)) //                stopLoading()
            return@checkEventStatus
        }

        requestSendTokenToEmail(email = email)
    }

    fun onVerifyAuthCode() = checkEventStatus(checkNetwork = true) {
        val email: String = username.value ?: return@checkEventStatus focusOn.postValue(InfoEnum.EMAIL)
        val authCode: String = authCode.value ?: return@checkEventStatus focusOn.postValue(InfoEnum.AUTH_CODE)

        verifyAuthCode(email = email, authCode = authCode).start()
    }

    fun onResetProcessClicked()
    {
        postNavigator(NavigatorConst.Event.INPUT_EMAIL_FOR_SEND)

        jwtToken = ""
        authCode.postValue("")
    }

    fun onResetPassword() = checkEventStatus(checkNetwork = true) {
        validity.forEach { (k, v) ->
            if(!v)
            {
                return@checkEventStatus _invalidity.postValue(Pair(k, v))
            }
        }


        val email: String = username.value ?: return@checkEventStatus focusOn.postValue(InfoEnum.EMAIL)
        val authCode: String = authCode.value ?: return@checkEventStatus focusOn.postValue(InfoEnum.AUTH_CODE)
        val password: String = password.value ?: return@checkEventStatus focusOn.postValue(InfoEnum.PASSWORD)

        val resetPassword = ResetPassword(resetToken = jwtToken, authCode = authCode, email = email, password = password.toMD5())
        requestResetPassword(resetPassword = resetPassword)
    }

    fun onConfirmResetPassword()
    {
        postNavigator(NavigatorConst.Event.COMPLETE)
    }

    private fun requestSendTokenToEmail(email: String) = scope.launch {
        try
        {
            onStartLoading()
            cnfOfFailureAuthCode = 0
            jwtToken = sendAuthTokenUseCase(username = email)
            postNavigator(NavigatorConst.Event.INPUT_AUTH_CODE)
        }
        finally
        {
            onStopLoading()
        }
    }

    private fun verifyAuthCode(email: String, authCode: String) = scope.launch {
        try
        {
            onStartLoading()
            verifyAuthTokenUseCase(ResetAuthCode(jwtToken, authCode, email))
            postNavigator(NavigatorConst.Event.INPUT_PASSWORD_FOR_RESET)

            validity.clear()
            validity[InfoEnum.PASSWORD] = false
        }
        finally
        {
            onStopLoading()
        }
    }

    private fun requestResetPassword(resetPassword: ResetPassword) = scope.launch {
        try
        {
            onStartLoading()
            resetPasswordUseCase(resetPassword = resetPassword)
            postNavigator(NavigatorConst.Event.COMPLETED_RESET_PASSWORD)
        }
        finally
        {
            onStopLoading()
        }
    }

    override fun handlerAPIException(exception: SOPOApiException)
    {
        super.handlerAPIException(exception)
        when(exception.code)
        {
            ErrorCode.VALIDATION -> postErrorSnackBar(exception.message)
            ErrorCode.INVALID_USER -> postErrorSnackBar(exception.message)
            ErrorCode.INVALID_AUTH_CODE ->
            {
                cnfOfFailureAuthCode += 1

                _focusOn.postValue(InfoEnum.AUTH_CODE)

                // TODO 실행되지 않음
                if(cnfOfFailureAuthCode >= 2)
                {
                    val comment = "인증코드 재발송"
                    val underlineComment = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    {
                        Html.fromHtml("<u>${comment}</u>", Html.FROM_HTML_MODE_LEGACY)
                    }
                    else
                    {
                        Html.fromHtml("<u>${comment}</u>")
                    }

                    postErrorSnackBar("인증 코드가 일치하지 않아요.", Pair(underlineComment, object: OnSnackBarClickListener<Unit> {
                        override fun invoke(data: Unit)
                        {
                            requestSendTokenToEmail(email = username.value?.toString() ?: "")
                        }
                    }))
                }
                else
                {
                    postErrorSnackBar("인증 코드가 일치하지 않아요.")
                }
            }
            ErrorCode.INVALID_JWT_TOKEN ->
            {
                postNavigator(NavigatorConst.Error.INVALID_JWT_TOKEN)
            }
            else ->
            {
                exception.printStackTrace()
                postErrorSnackBar("[불명]${exception.message}")
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