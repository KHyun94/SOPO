package com.delivery.sopo.viewmodels.menus

import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.ParcelExceptionHandler
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers

class SignOutViewModel(
        private val userRemoteRepo: UserRemoteRepository,
): BaseViewModel()
{
    private var preCheckBox: AppCompatCheckBox? = null
    private var currentCheckBox: AppCompatCheckBox? = null

    private var _result = MutableLiveData<ResponseResult<*>>()
    val result: LiveData<ResponseResult<*>>
        get() = _result

    var message = MutableLiveData<String>()

    var otherReason = MutableLiveData<String>()
    var isOtherReasonEt = MutableLiveData<Boolean>()

    init
    {
        isOtherReasonEt.value = false
    }

    fun onBackClicked()
    {

    }

    fun onCheckClicked(v: View, message: String?)
    { //        v.requestFocusFromTouch()
        v.requestFocus()
        SopoLog.d("Sign Out Message >>> $message")

        currentCheckBox = v as AppCompatCheckBox

        // 이미 선택되어있는 체크박스와 현재 선택한 체크박스가 다르면 이전 체크박스를 해제한다.
        if(currentCheckBox != preCheckBox)
        {
            preCheckBox?.isChecked = false
        }

        isOtherReasonEt.value =
            currentCheckBox!!.id == R.id.cb_reason6 && currentCheckBox!!.isChecked == true

        if(isOtherReasonEt.value == false)
        {
            this.otherReason.value = ""
        }

        this.message.value = if(!currentCheckBox!!.isChecked) "" else message

        preCheckBox = currentCheckBox
    }

    fun onSignOutClicked()
    {
        if(message.value.toString() == "") return SopoLog.e("Message is null or empty")

        _result.postValue(ResponseResult(true, ResponseCode.SUCCESS, message.value.toString(), "SUCCESS", DisplayEnum.DIALOG))
    }

    suspend fun requestSignOut(reason: String)
    {
        val res = userRemoteRepo.requestSignOut(reason)

//        if(!res.result)
//        {
//            SopoLog.e("계정 탈퇴 >>> ${res.code} / ${res.message}")
//            _result.postValue(ResponseResult(false, res.code, null, res.message, DisplayEnum.DIALOG))
//            return
//        }
//
//        SopoLog.d("계정 탈퇴 완료")
//        _result.postValue(ResponseResult(true, ResponseCode.SUCCESS, null, "SUCCESS", DisplayEnum.TOAST_MESSAGE))
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
            postErrorSnackBar("알 수 없는 이유로 등록에 실패했습니다.[${error.toString()}]")
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
        ParcelExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }
}