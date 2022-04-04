package com.delivery.sopo.viewmodels.menus

import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.ParcelExceptionHandler
import com.delivery.sopo.exceptions.UserExceptionHandler
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.usecase.user.SignOutUseCase
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
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

    fun setNavigator(navigator: String){ _navigator.postValue(navigator) }

    init
    {
        message.value = ""
        isOtherReasonEt.value = false
    }

    fun onBackClicked()
    {
        setNavigator(NavigatorConst.TO_BACK_SCREEN)
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

        setNavigator(NavigatorConst.CONFIRM_SIGN_OUT)
    }

    fun requestSignOut(reason: String) = scope.launch(Dispatchers.IO){
        try
        {
            onStartLoading()
            signOutUseCase.invoke(reason)
            setNavigator(NavigatorConst.EXIT)
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }
        finally
        {
            onStopLoading()
        }
    }

    private val onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onRegisterParcelError(error: ErrorEnum)
        {
            super.onRegisterParcelError(error)

            postErrorSnackBar(error.message)
        }

        override fun onFailure(error: ErrorEnum)
        {
            postErrorSnackBar("알 수 없는 이유로 탈퇴에 실패했습니다.[${error.toString()}]")
        }

        override fun onInternalServerError(error: ErrorEnum)
        {
            super.onInternalServerError(error)

            postErrorSnackBar("일시적으로 서비스를 이용할 수 없습니다.[${error.toString()}]")
        }

        override fun onAuthError(error: ErrorEnum)
        {
            super.onAuthError(error)

            postErrorSnackBar("유저 인증에 실패했습니다. 다시 시도해주세요.[${error.toString()}]")
        }

        override fun onDuplicateError(error: ErrorEnum)
        {
            super.onDuplicateError(error)
            moveDuplicated()
        }
    }
    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        UserExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }
}