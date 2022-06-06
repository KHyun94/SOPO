package com.delivery.sopo.presentation.viewmodels.menus

import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.R
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.domain.usecase.user.token.SignOutUseCase
import kotlinx.coroutines.launch

class SignOutViewModel(
        private val signOutUseCase: SignOutUseCase,
): BaseViewModel()
{
    private var preCheckBox: AppCompatCheckBox? = null
    private var currentCheckBox: AppCompatCheckBox? = null

    var message = MutableLiveData<String>()

    var otherReason = MutableLiveData<String>()
    var isOtherReasonEt = MutableLiveData<Boolean>()

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    fun postNavigator(navigator: String){ _navigator.postValue(navigator) }

    init
    {
        message.value = ""
        isOtherReasonEt.value = false
    }

    fun onBackClicked()
    {
        postNavigator(NavigatorConst.Event.BACK)
    }

    fun onCheckClicked(v: View, message: String?) = checkEventStatus {

        currentCheckBox = v as AppCompatCheckBox

        // 이미 선택되어있는 체크박스와 현재 선택한 체크박스가 다르면 이전 체크박스를 해제한다.
        if(currentCheckBox != preCheckBox)
        {
            preCheckBox?.isChecked = false
        }

        isOtherReasonEt.value = (currentCheckBox?.id == R.id.cb_reason6) && (currentCheckBox?.isChecked == true)

        if(isOtherReasonEt.value == false)
        {
            this.otherReason.value = ""
        }

        this.message.value = if((currentCheckBox?.isChecked) == false) "" else message

        preCheckBox = currentCheckBox
    }

    fun onSignOutClicked() = checkEventStatus(checkNetwork = true){

        val reason = message.value.toString()
        if(reason == "") return@checkEventStatus postErrorSnackBar("탈퇴 사유를 선택해주세요.")

        postNavigator(NavigatorConst.CONFIRM_SIGN_OUT)
    }

    fun requestSignOut(reason: String) = scope.launch(coroutineExceptionHandler){
        try
        {
            onStartLoading()
            signOutUseCase.invoke(reason)
            postNavigator(NavigatorConst.EXIT)
        }
        finally
        {
            onStopLoading()
        }
    }

    override var onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onRegisterParcelError(error: ErrorCode)
        {
            super.onRegisterParcelError(error)

            postErrorSnackBar(error.message)
        }

        override fun onFailure(error: ErrorCode)
        {
            postErrorSnackBar("알 수 없는 이유로 탈퇴에 실패했습니다.[${error.toString()}]")
        }

        override fun onInternalServerError(error: ErrorCode)
        {
            super.onInternalServerError(error)

            postErrorSnackBar("일시적으로 서비스를 이용할 수 없습니다.[${error.toString()}]")
        }

        override fun onAuthError(error: ErrorCode)
        {
            super.onAuthError(error)

            postErrorSnackBar("유저 인증에 실패했습니다. 다시 시도해주세요.[${error.toString()}]")
        }

        override fun onDuplicateError(error: ErrorCode)
        {
            super.onDuplicateError(error)
            moveDuplicated()
        }
    }
}