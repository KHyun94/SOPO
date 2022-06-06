package com.delivery.sopo.presentation.viewmodels.menus

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.presentation.bindings.FocusChangeCallback
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.data.repositories.local.user.UserLocalRepository
import com.delivery.sopo.data.repositories.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.domain.usecase.user.UpdateNicknameUseCase
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.launch

class UpdateNicknameViewModel(private val userLocalRepo: UserLocalRepository,
                              private val userRemoteRepo: UserRemoteRepository,
                              private val updateNicknameUseCase: UpdateNicknameUseCase):
        BaseViewModel()
{
    val currentNickname = MutableLiveData<String>().apply {
        postValue(userLocalRepo.getNickname())
    }

    val nickname = MutableLiveData<String>()
    val validates = mutableMapOf<InfoEnum, Boolean>()

    private var _validateError = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val validateError: LiveData<Pair<InfoEnum, Boolean>>
        get() = _validateError

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: MutableLiveData<Triple<View, Boolean, InfoEnum>>
        get() = _focus

    val focusChangeCallback: FocusChangeCallback = FocusChangeCallback@{ v, hasFocus, type ->
        SopoLog.i("${type.NAME} >>> $hasFocus")
        _focus.value = (Triple(v, hasFocus, type))
    }

    // 유효성 및 통신 등의 결과 객체
    private var _result = MutableLiveData<ResponseResult<*>>()
    val result: LiveData<ResponseResult<*>>
        get() = _result

    override var onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onFailure(error: ErrorCode)
        { // TODO 발생하는 에러가 있을까?
            //            postErrorSnackBar("로그인에 실패했습니다.")
        }

        override fun onLoginError(error: ErrorCode)
        {
            super.onLoginError(error)
        }

        override fun onInternalServerError(error: ErrorCode)
        {
            super.onInternalServerError(error)
            postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
        }
    }

    init
    {
        validates[InfoEnum.NICKNAME] = false
    }

    fun onClearClicked()
    {
        _navigator.postValue(NavigatorConst.TO_BACK_SCREEN)
    }

    fun onUpdateNicknameClicked(v: View) = checkEventStatus(true, 100) {
        SopoLog.d("onCompleteSignUpClicked()")
        validates.forEach { (k, v) ->
            if(!v)
            {
                SopoLog.d("${k.NAME} validate is fail")
                return@checkEventStatus _validateError.postValue(Pair(k, v))
            }
        }

        updateNickname(nickname.value ?: "")
    }

    private fun updateNickname(nickname: String) = scope.launch(coroutineExceptionHandler) {

        try
        {
            onStartLoading()
            updateNicknameUseCase.invoke(nickname = nickname)
            _navigator.postValue(NavigatorConst.Screen.MAIN)
        }
        finally
        {
            onStopLoading()
        }
    }
}