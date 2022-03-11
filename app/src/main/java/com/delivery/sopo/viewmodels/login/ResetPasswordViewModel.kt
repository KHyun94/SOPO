package com.delivery.sopo.viewmodels.login

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.LockStatusConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.ResetPasswordConst
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.exceptions.UserExceptionHandler
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.user.ResetPassword
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog
import com.kakao.util.KakaoParameterException
import kotlinx.coroutines.*

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

    fun setNavigator(navigator: String)
    {
        _navigator.postValue(navigator)
    }

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: MutableLiveData<Triple<View, Boolean, InfoEnum>>
        get() = _focus

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
                    _invalidity.postValue(Pair(InfoEnum.EMAIL, false))
                    postErrorSnackBar(error.message)
                }
                ErrorEnum.INVALID_JWT_TOKEN ->
                {
                    postErrorSnackBar("일정시간이 지났기 때문에 다시 시도해주세요.")
                    //TODO JWT_TOKEN 만료 시 안내와 동시에 처음부터 시작
                    setNavigator(ResetPasswordConst.INPUT_EMAIL_FOR_SEND)
                    jwtToken = ""
                    authToken = ""
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
        SopoLog.i("onSendEmailClicked() 호출 [data:${navigator.value}]")

        startLoading()

        validity.forEach { (k, v) ->
            if(!v)
            {
                stopLoading()
                return@checkEventStatus _invalidity.postValue(Pair(k, v))
            }
        }

        when(navigator.value)
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

        }
    }

//    fun onSendEmailClicked(v: View) = checkEventStatus(checkNetwork = true) {
//        SopoLog.i("onSendEmailClicked() 호출 [data:${navigator.value}]")
//
//        startLoading()
//
//        validity.forEach { (k, v) ->
//            if(!v)
//            {
//                stopLoading()
//                return@checkEventStatus _invalidity.postValue(Pair(k, v))
//            }
//        }
//
//        when(navigator.value)
//        {
//            ResetPasswordConst.INPUT_EMAIL_FOR_SEND ->
//            {
//                val email = email.value?.toString()
//
//                if(email == null)
//                {
//                    postErrorSnackBar("이메일을 다시 입력해주세요.")
//                    _invalidity.postValue(Pair(InfoEnum.EMAIL, false))
//                    stopLoading()
//                    return@checkEventStatus
//                }
//
//                requestSendTokenToEmail(email = email)
//            }
//            ResetPasswordConst.INPUT_PASSWORD_FOR_RESET ->
//            {
//                val password = password.value?.toString()
//
//                if(password == null)
//                {
//                    postErrorSnackBar("이메일을 다시 입력해주세요.")
//                    _invalidity.postValue(Pair(InfoEnum.PASSWORD, false))
//                    stopLoading()
//                    return@checkEventStatus
//                }
//
//                val resetPassword = ResetPassword(jwtToken, authToken, email.value.toString(), password)
//                requestResetPassword(resetPassword = resetPassword)
//                stopLoading()
//            }
//            ResetPasswordConst.COMPLETED_RESET_PASSWORD ->
//            {
//                _navigator.postValue(NavigatorConst.TO_COMPLETE)
//                stopLoading()
//            }
//
//        }
//    }

    private fun requestSendTokenToEmail(email: String) = scope.launch(Dispatchers.IO) {
        try
        {
            jwtToken = userRemoteRepo.requestSendTokenToEmail(email = email)
            stopLoading()
            setNavigator(ResetPasswordConst.SEND_AUTH_EMAIL)
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }
    }

    private fun requestResetPassword(resetPassword: ResetPassword) = scope.launch (Dispatchers.IO) {
        try
        {
            userRemoteRepo.requestResetPassword(resetPassword = resetPassword)
            stopLoading()
            setNavigator(ResetPasswordConst.COMPLETED_RESET_PASSWORD)
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }
    }




}