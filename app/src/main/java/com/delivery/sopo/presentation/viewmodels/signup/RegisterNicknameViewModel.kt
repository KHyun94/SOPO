package com.delivery.sopo.presentation.viewmodels.signup

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.presentation.bindings.FocusChangeCallback
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.domain.usecase.user.UpdateNicknameUseCase
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class RegisterNicknameViewModel(private val updateNicknameUseCase: UpdateNicknameUseCase): BaseViewModel()
{
    val nickname = MutableLiveData<String>()

    val validates = mutableMapOf<InfoEnum, Boolean>()

    private var _validateError = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val validateError: LiveData<Pair<InfoEnum, Boolean>> = _validateError

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: MutableLiveData<Triple<View, Boolean, InfoEnum>> = _focus

    val focusChangeCallback: FocusChangeCallback = FocusChangeCallback@{ v, hasFocus, type ->
        SopoLog.i("${type.NAME} >>> $hasFocus")
        _focus.value = (Triple(v, hasFocus, type))
    }

    // 유효성 및 통신 등의 결과 객체
    private var _navigator = MutableLiveData<String>()
    val navigator: LiveData<String> = _navigator

    fun postNavigator(navigator: String) = _navigator.postValue(navigator)

    init
    {
        validates[InfoEnum.NICKNAME] = false
    }

    fun onRegisterNicknameClicked() = checkEventStatus(checkNetwork = true) {

        validates.forEach { (k, v) ->
            if(!v)
            {
                return@checkEventStatus _validateError.postValue(Pair(k, v))
            }
        }

        val nickname = nickname.value ?: return@checkEventStatus _validateError.postValue(Pair(InfoEnum.NICKNAME, false))

        updateNickname(nickname = nickname)
    }

    private fun updateNickname(nickname: String) = scope.launch(coroutineExceptionHandler) {
        try
        {
            onStartLoading()
            updateNicknameUseCase(nickname = nickname)
            postNavigator(NavigatorConst.Screen.MAIN)
        }
        finally
        {
            onStopLoading()
        }
    }

    val exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { coroutineContext, throwable ->
            when(throwable)
            {
                is SOPOApiException -> handlerAPIException(throwable)
                is InternalServerException -> postErrorSnackBar(throwable.message)
                else ->
                {
                    throwable.printStackTrace()
                    postErrorSnackBar(throwable.message ?: "확인할 수 없는 에러입니다.")
                }
            }
        }

    private fun handlerAPIException(exception: SOPOApiException)
    {
        when(exception.code)
        {
            ErrorCode.VALIDATION -> postErrorSnackBar(exception.message)
            else ->
            {
                exception.printStackTrace()
                postErrorSnackBar("[불명]${exception.message}")
            }
        }
    }

    /** 삭제 예정 */
    override var onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onFailure(error: ErrorCode)
        {
        }

        override fun onInternalServerError(error: ErrorCode)
        {
            super.onInternalServerError(error)

            postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
        }

        override fun onAuthError(error: ErrorCode)
        {
            super.onAuthError(error)
        }
    }

}