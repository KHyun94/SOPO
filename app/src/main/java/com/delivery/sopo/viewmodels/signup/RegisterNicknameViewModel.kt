package com.delivery.sopo.viewmodels.signup

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.NavigatorEnum
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.usecase.UpdateNicknameUseCase
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.launch

class RegisterNicknameViewModel(private val updateNicknameUseCase: UpdateNicknameUseCase): BaseViewModel()
{
    val nickname = MutableLiveData<String>()
    val validates = mutableMapOf<InfoEnum, Boolean>()

    private var _validateError = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val validateError: LiveData<Pair<InfoEnum, Boolean>>
        get() = _validateError

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: MutableLiveData<Triple<View, Boolean, InfoEnum>>
        get() = _focus

    val focusChangeCallback: FocusChangeCallback = FocusChangeCallback@{ v, hasFocus, type ->
        SopoLog.i("${type.NAME} >>> $hasFocus")
        _focus.value = (Triple(v, hasFocus, type))
    }

    // 유효성 및 통신 등의 결과 객체
    private var _navigator = MutableLiveData<NavigatorEnum>()
    val navigator: LiveData<NavigatorEnum>
        get() = _navigator

    override var onSOPOErrorCallback = object: OnSOPOErrorCallback
    {

        override fun onFailure(error: ErrorEnum)
        {
        }

        override fun onInternalServerError(error: ErrorEnum)
        {
            super.onInternalServerError(error)

            postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
        }

        override fun onAuthError(error: ErrorEnum)
        {
            super.onAuthError(error)
        }
    }

    init
    {
        validates[InfoEnum.NICKNAME] = false
    }

    fun onRegisterNicknameClicked() = checkEventStatus(checkNetwork = true) {
        SopoLog.d("onCompleteSignUpClicked()")
        validates.forEach { (k, v) ->
            if(!v)
            {
                SopoLog.d("${k.NAME} validate is fail")
                _validateError.postValue(Pair(k, v))
                return@checkEventStatus
            }
        }

        scope.launch(coroutineExceptionHandler) {
            try
            {
                onStartLoading()
                updateNicknameUseCase.invoke(nickname = nickname.value ?: "")
                _navigator.postValue(NavigatorEnum.MAIN)
            }
            finally
            {
                onStopLoading()
            }
        }
    }
}