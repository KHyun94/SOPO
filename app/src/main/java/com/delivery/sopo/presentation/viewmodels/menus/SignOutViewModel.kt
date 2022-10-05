package com.delivery.sopo.presentation.viewmodels.menus

import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.R
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.domain.usecase.user.token.SignOutUseCase
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.SOPOApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignOutViewModel @Inject constructor(
        private val signOutUseCase: SignOutUseCase,
): BaseViewModel()
{
    private var preCheckBox: AppCompatCheckBox? = null
    private var currentCheckBox: AppCompatCheckBox? = null

    var message = MutableLiveData<String>()

    var otherReason = MutableLiveData<String>()
    var isOtherReasonEt = MutableLiveData<Boolean>()

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String> = _navigator

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

    fun requestSignOut(reason: String) = scope.launch{
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

    override fun handlerAPIException(exception: SOPOApiException)
    {
        super.handlerAPIException(exception)
        postErrorSnackBar("알 수 없는 이유로 탈퇴에 실패했습니다.[${exception.toString()}]")
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