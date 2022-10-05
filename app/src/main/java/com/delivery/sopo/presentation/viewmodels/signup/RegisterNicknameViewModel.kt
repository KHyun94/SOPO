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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterNicknameViewModel @Inject constructor(
        private val updateNicknameUseCase: UpdateNicknameUseCase
        ): BaseViewModel()
{
    val nickname = MutableLiveData<String>()

    // 유효성 및 통신 등의 결과 객체
    private var _navigator = MutableLiveData<String>()
    val navigator: LiveData<String> = _navigator

    fun postNavigator(navigator: String) = _navigator.postValue(navigator)


    fun onRegisterNicknameClicked() = checkEventStatus(checkNetwork = true) {

        val nickname = nickname.value

        SopoLog.d("테스트 닉네임 $nickname")

        updateNickname(nickname = nickname?:"")
    }

    private fun updateNickname(nickname: String) = scope.launch {
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

    override fun handlerAPIException(exception: SOPOApiException)
    {
        super.handlerAPIException(exception)
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