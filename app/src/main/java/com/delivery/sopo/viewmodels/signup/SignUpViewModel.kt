package com.delivery.sopo.viewmodels.signup

import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.exceptions.UserExceptionHandler
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.JoinRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.networks.dto.joins.JoinInfo
import com.delivery.sopo.networks.repository.JoinRepositoryImpl
import kotlinx.coroutines.*


class SignUpViewModel(
        private val userLocalRepo: UserLocalRepository,
        private val joinRepoImpl: JoinRepositoryImpl):
        BaseViewModel()
{
    private val joinRepo: JoinRepository = joinRepoImpl

    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val rePassword = MutableLiveData<String>()

    val validity = mutableMapOf<InfoEnum, Boolean>()

    private var _invalidity = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val invalidity: LiveData<Pair<InfoEnum, Boolean>>
        get() = _invalidity

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: MutableLiveData<Triple<View, Boolean, InfoEnum>>
        get() = _focus

    var focusChangeCallback: FocusChangeCallback = FocusChangeCallback@{ v, hasFocus, type ->
        _focus.value = (Triple(v, hasFocus, type))
    }

    private var _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    private val onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onFailure(error: ErrorEnum)
        {
            postErrorSnackBar("로그인에 실패했습니다.")
        }
        override fun onSignUpError(error: ErrorEnum)
        {
            super.onSignUpError(error)
            postErrorSnackBar("이미 등록된 사용자입니다.")
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
        initValidity()
    }

    private fun initValidity()
    {
        validity[InfoEnum.EMAIL] = false
        validity[InfoEnum.PASSWORD] = false
        validity[InfoEnum.RE_PASSWORD] = false
        validity[InfoEnum.AGREEMENT] = false
    }

    // 개인정보 동의 이벤트
    fun onAgreeClicked(v: View)
    {
        val checkBox = v as AppCompatCheckBox
        validity[InfoEnum.AGREEMENT] = checkBox.isChecked
    }

    fun onSignUpClicked(v: View) = checkEventStatus(checkNetwork = true)
    {
        startLoading()
        // 입력값의 유효 처리 여부 확인
        validity.forEach { (k, v) ->
            if(!v)
            {
                stopLoading()
                return@checkEventStatus _invalidity.postValue(Pair(k, v))
            }
        }

        val email = email.value.toString().trim()
        val password = password.value.toString().trim()

        val joinInfo = JoinInfo(email = email, password = password.toMD5())

        requestSignUp(joinInfo = joinInfo)
    }

    private fun requestSignUp(joinInfo: JoinInfo) = scope.launch(Dispatchers.IO) {

        try
        {
            joinRepo.requestJoinBySelf(joinInfo)

            withContext(Dispatchers.Default) {
                userLocalRepo.setUserId(joinInfo.email)
                userLocalRepo.setUserPassword(joinInfo.password)
            }

            _navigator.postValue(NavigatorConst.TO_COMPLETE)
        }
        catch(e: Exception)
        {
            return@launch exceptionHandler.handleException(coroutineContext, e)
        }
        finally
        {
            stopLoading()
        }
    }
}