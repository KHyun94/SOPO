package com.delivery.sopo.presentation.viewmodels.signup

import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.UserTypeConst
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.data.networks.dto.joins.JoinInfo
import com.delivery.sopo.domain.usecase.user.token.SignUpUseCase
import kotlinx.coroutines.launch


class SignUpViewModel(private val signUpUseCase: SignUpUseCase): BaseViewModel()
{
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

    fun postNavigator(navigator: String)
    {
        _navigator.postValue(navigator)
    }

    override var onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onFailure(error: ErrorEnum)
        {
            postErrorSnackBar("로그인에 실패했습니다.")
        }

        override fun onAlreadyRegisteredUser(error: ErrorEnum)
        {
            super.onAlreadyRegisteredUser(error)
            postErrorSnackBar("이미 등록된 사용자입니다.")
        }

        override fun onInternalServerError(error: ErrorEnum)
        {
            super.onInternalServerError(error)
            postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
        }
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

    fun onSignUpClicked() = checkEventStatus(checkNetwork = true) { // 입력값의 유효 처리 여부 확인
        validity.forEach { (k, v) ->
            if(!v) return@checkEventStatus _invalidity.postValue(Pair(k, v))
        }

        val email = email.value.toString().trim()
        val password = password.value.toString().trim()
        val joinInfo = JoinInfo(email = email, password = password.toMD5())

        requestSignUp(joinInfo = joinInfo)
    }

    private fun requestSignUp(joinInfo: JoinInfo) = scope.launch(coroutineExceptionHandler) {
        try
        {
            onStartLoading()
            signUpUseCase.invoke(joinInfo = joinInfo, userType = UserTypeConst.SELF)
            postNavigator(NavigatorConst.TO_COMPLETE)
        }
        finally
        {
            onStopLoading()
        }
    }
}