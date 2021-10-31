package com.delivery.sopo.viewmodels.menus

import android.os.Handler
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.UserExceptionHandler
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateNicknameViewModel(private val userLocalRepo: UserLocalRepository, private val userRemoteRepo: UserRemoteRepository):
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

    private val onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onFailure(error: ErrorEnum)
        {
            // TODO 발생하는 에러가 있을까?
            //            postErrorSnackBar("로그인에 실패했습니다.")
        }

        override fun onLoginError(error: ErrorEnum)
        {
            super.onLoginError(error)
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

    init
    {
        validates[InfoEnum.NICKNAME] = false
    }

    fun onClearClicked()
    {
        _navigator.postValue(NavigatorConst.TO_BACK_SCREEN)
    }

    fun onCompleteSignUpClicked(v: View)
    {
        v.requestFocusFromTouch()
        SopoLog.d("onCompleteSignUpClicked()")
        validates.forEach { (k, v) ->
            if(!v)
            {
                SopoLog.d("${k.NAME} validate is fail")
                return _validateError.postValue(Pair(k, v))
            }
        }

        try
        {
            updateNickname(nickname = nickname.value ?: "")
            requestLogin()
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(scope.coroutineContext, e)
        }
    }

    private fun updateNickname(nickname: String) = scope.launch(Dispatchers.IO) {

        userRemoteRepo.updateNickname(nickname = nickname)
        _navigator.postValue(NavigatorConst.TO_MAIN)

    }

    private fun requestLogin() = scope.launch(Dispatchers.IO) {
        val email = userLocalRepo.getUserId()
        val password = userLocalRepo.getUserPassword()
        userRemoteRepo.requestLogin(email, password)
    }
}