package com.delivery.sopo.viewmodels.login

import android.os.Build
import android.text.Html
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.ResetPasswordConst
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.exceptions.UserExceptionHandler
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.user.ResetAuthCode
import com.delivery.sopo.models.user.ResetPassword
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.OnSnackBarClickListener
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResetPasswordViewModel(private val userRemoteRepo: UserRemoteRepository): BaseViewModel()
{
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    val validity = mutableMapOf<InfoEnum, Boolean>()

    private var _invalidity = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val invalidity: LiveData<Pair<InfoEnum, Boolean>>
        get() = _invalidity

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    val authCode = MutableLiveData<String>()

    fun setNavigator(navigator: String)
    {
        _navigator.postValue(navigator)
    }

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: MutableLiveData<Triple<View, Boolean, InfoEnum>>
        get() = _focus

    private val _focusOn = MutableLiveData<InfoEnum>()
    val focusOn: MutableLiveData<InfoEnum>
        get() = _focusOn

    var cnfOfFailureAuthCode: Int = 0

    val focusChangeCallback: FocusChangeCallback = FocusChangeCallback@{ v, hasFocus, type ->
        _focus.value = (Triple(v, hasFocus, type))
    }

    private val onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onFailure(error: ErrorEnum)
        {
            stopLoading()

            when(error)
            {
                ErrorEnum.INVALID_USER ->
                {
                    _focusOn.postValue(InfoEnum.EMAIL)
                    postErrorSnackBar(error.message)
                }
                ErrorEnum.INVALID_AUTH_CODE ->
                {
                    cnfOfFailureAuthCode += 1

                    _focusOn.postValue(InfoEnum.AUTH_CODE)

                    if(cnfOfFailureAuthCode >= 2)
                    {
                        val comment = "인증코드 재발송"
                        val underlineComment = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { Html.fromHtml("<u>${comment}</u>", Html.FROM_HTML_MODE_LEGACY) } else { Html.fromHtml("<u>${comment}</u>") }

                        postErrorSnackBar("인증 코드가 일치하지 않아요.", Pair(underlineComment, object : OnSnackBarClickListener{
                            override fun invoke()
                            {
                                requestSendTokenToEmail(email = email.value?.toString()?:"")
                            }

                        }))
                    }
                    else
                    {
                        postErrorSnackBar("인증 코드가 일치하지 않아요.")
                    }
                }
                ErrorEnum.INVALID_JWT_TOKEN ->
                {
                    postErrorSnackBar("일정시간이 지났기 때문에 다시 시도해주세요.") //TODO JWT_TOKEN 만료 시 안내와 동시에 처음부터 시작
                    setNavigator(ResetPasswordConst.INPUT_EMAIL_FOR_SEND)

                    jwtToken = ""
                    authCode.postValue("")
                }
                else ->
                {
                    postErrorSnackBar(error.message)
                }
            }
        }

        override fun onInternalServerError(error: ErrorEnum)
        {
            super.onInternalServerError(error)
            postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
        }
    }

    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        UserExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }

    var jwtToken: String = ""
    var authToken: String = ""

    init
    {
        validity[InfoEnum.EMAIL] = false
        setNavigator(ResetPasswordConst.INPUT_EMAIL_FOR_SEND)
    }

    fun onClearClicked()
    {
        setNavigator(NavigatorConst.TO_BACK_SCREEN)
    }

    fun onSendEmailClicked(v: View) = checkEventStatus(checkNetwork = true) {
        SopoLog.i("onSendEmailClicked() 호출")

        startLoading()

        validity.forEach { (k, v) ->
            if(!v)
            {
                stopLoading()
                return@checkEventStatus _invalidity.postValue(Pair(k, v))
            }
        }

        val email = email.value?.toString()

        if(email == null)
        {
            postErrorSnackBar("이메일을 다시 입력해주세요.")
            _invalidity.postValue(Pair(InfoEnum.EMAIL, false)) //                stopLoading()
            return@checkEventStatus
        }

        requestSendTokenToEmail(email = email)


        /*when(navigator.value)
        {
            ResetPasswordConst.INPUT_EMAIL_FOR_SEND ->
            {
                val email = email.value?.toString()

                if(email == null)
                {
                    postErrorSnackBar("이메일을 다시 입력해주세요.")
                    _invalidity.postValue(Pair(InfoEnum.EMAIL, false))
                    stopLoading()
                    return@checkEventStatus
                }

                requestSendTokenToEmail(email = email)
            }
            ResetPasswordConst.INPUT_PASSWORD_FOR_RESET ->
            {
                val password = password.value?.toString()

                if(password == null)
                {
                    postErrorSnackBar("이메일을 다시 입력해주세요.")
                    _invalidity.postValue(Pair(InfoEnum.PASSWORD, false))
                    stopLoading()
                    return@checkEventStatus
                }

                val resetPassword = ResetPassword(jwtToken, authToken, email.value.toString(), password)
                requestResetPassword(resetPassword = resetPassword)
                stopLoading()
            }
            ResetPasswordConst.COMPLETED_RESET_PASSWORD ->
            {
                _navigator.postValue(NavigatorConst.TO_COMPLETE)
                stopLoading()
            }

        }*/
    }

    fun onVerifyAuthCode() = checkEventStatus(checkNetwork = true)
    {
        startLoading()

        try
        {
            val email: String = email.value ?: return@checkEventStatus focusOn.postValue(InfoEnum.EMAIL)
            val authCode: String = authCode.value ?: return@checkEventStatus focusOn.postValue(InfoEnum.AUTH_CODE)

            verifyAuthCode(email = email, authCode = authCode).start()
        }
        finally
        {
            stopLoading()
        }
    }

    fun onResetPassword() = checkEventStatus(checkNetwork = true)
    {
        startLoading()

        validity.forEach { (k, v) ->
            if(!v)
            {
                stopLoading()
                return@checkEventStatus _invalidity.postValue(Pair(k, v))
            }
        }

        try
        {
            val email: String = email.value ?: return@checkEventStatus focusOn.postValue(InfoEnum.EMAIL)
            val authCode: String = authCode.value ?: return@checkEventStatus focusOn.postValue(InfoEnum.AUTH_CODE)
            val password: String = password.value ?: return@checkEventStatus focusOn.postValue(InfoEnum.PASSWORD)

            val resetPassword = ResetPassword(resetToken = jwtToken, authCode = authCode, email = email, password = password)
            requestResetPassword(resetPassword = resetPassword)
        }
        finally
        {
            stopLoading()
        }
    }

    fun onConfirmResetPassword(){
        setNavigator(NavigatorConst.TO_COMPLETE)
    }

    private fun verifyAuthCode(email: String, authCode: String) = scope.launch(Dispatchers.IO) {
        try
        {
            userRemoteRepo.requestVerifyAuthToken(ResetAuthCode(jwtToken, authCode, email))
            setNavigator(ResetPasswordConst.INPUT_PASSWORD_FOR_RESET)

            validity.clear()
            validity[InfoEnum.PASSWORD] = false
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }
        finally
        {
            stopLoading()
        }
    }

    private fun requestSendTokenToEmail(email: String) = scope.launch(Dispatchers.IO) {
        try
        {
            cnfOfFailureAuthCode = 0
            jwtToken = userRemoteRepo.requestSendTokenToEmail(email = email)
            setNavigator(ResetPasswordConst.INPUT_AUTH_CODE)
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }
        finally
        {
            stopLoading()
        }
    }

    private fun requestResetPassword(resetPassword: ResetPassword) = scope.launch(Dispatchers.IO) {
        try
        {
            userRemoteRepo.requestResetPassword(resetPassword = resetPassword)
            setNavigator(ResetPasswordConst.COMPLETED_RESET_PASSWORD)
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }
        finally
        {
            stopLoading()
        }
    }


}