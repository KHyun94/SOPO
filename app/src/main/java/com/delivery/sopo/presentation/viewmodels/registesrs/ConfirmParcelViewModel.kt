package com.delivery.sopo.presentation.viewmodels.registesrs

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.R
import com.delivery.sopo.data.models.Result
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.domain.usecase.parcel.remote.RegisterParcelUseCase
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.BottomNotificationBar
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConfirmParcelViewModel(
        private val registerParcelUseCase: RegisterParcelUseCase
        ): BaseViewModel()
{
    lateinit var registerInfo: Parcel.Register

    val waybillNum = MutableLiveData<String>()
    val carrier = MutableLiveData<Carrier>()
    val alias = MutableLiveData<String>()

    lateinit var parcel: Parcel.Common

    private var _navigator = MutableLiveData<String>()
    val navigator: LiveData<String> = _navigator

    private var _status: MutableStateFlow<Result<Parcel.Common>> = MutableStateFlow(Result.Uninitialized)
    val status = _status.asStateFlow()

    private var _snackBar = MutableLiveData<BottomNotificationBar>()

    fun emitStatus(result: Result<Parcel.Common>) = scope.launch {
        _status.emit(result)
    }

    fun onMoveClicked(v: View) = checkEventStatus(checkNetwork = true) {

        when(v.id)
        {
            R.id.iv_revise ->
            {
                _navigator.value = NavigatorConst.REGISTER_REVISE
            }
            R.id.tv_init ->
            {
                _navigator.value = NavigatorConst.REGISTER_INITIALIZE
            }
            R.id.tv_register ->
            {

                if(alias.value.toString() == "null") alias.value = ""

                val registerDTO = Parcel.Register(carrier = carrier.value?.carrier
                    ?: throw Exception("Carrier must be not null"), waybillNum = waybillNum.value.toString(), alias = alias.value.toString())

                requestParcelRegister(register = registerDTO)
            }
        }
    }

    // '등록하기' Button Click event
    private fun requestParcelRegister(register: Parcel.Register) = scope.launch(coroutineExceptionHandler) {
        SopoLog.i("requestParcelRegister(...) 호출[${register.toString()}]")

        emitStatus(Result.Loading)
        parcel = registerParcelUseCase(register)

        delay(2000)

        emitStatus(Result.Success(parcel))
    }

    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->

        emitStatus(Result.Error(throwable))

        when(throwable)
        {
            is SOPOApiException -> handlerAPIException(throwable)
            is InternalServerException -> handlerInternalServerException(throwable)
            else ->
            {
                throwable.printStackTrace()
                postErrorSnackBar(throwable.message ?: "확인할 수 없는 에러입니다.")
            }
        }
    }

    private fun handlerAPIException(exception: SOPOApiException)
    {
        when(exception.code)
        {
            ErrorCode.ALREADY_REGISTERED_PARCEL, ErrorCode.OVER_REGISTERED_PARCEL, ErrorCode.PARCEL_BAD_REQUEST ->
                postErrorSnackBar(exception.message)
            else ->
            {
                exception.printStackTrace()
                postErrorSnackBar("[불명]${exception.message}")
            }
        }
    }

    /**
     *
     */
    private fun handlerInternalServerException(exception: InternalServerException)
    {
        when(val code = ErrorCode.getCode(exception.getErrorResponse().code))
        {
            ErrorCode.FAIL_TO_SEARCH_PARCEL -> postErrorSnackBar("택배사가 이상한가봐요?")
            else ->
            {
                exception.printStackTrace()
                postErrorSnackBar(exception.message)
            }
        }
    }

    override var onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onRegisterParcelError(error: ErrorCode)
        {
            super.onRegisterParcelError(error)
            postErrorSnackBar(error.message)
        }

        override fun onInquiryParcelError(error: ErrorCode)
        {
            super.onInquiryParcelError(error)
        }

        override fun onInternalServerError(error: ErrorCode)
        {
            super.onInternalServerError(error)
            postErrorSnackBar("일시적으로 서비스를 이용할 수 없습니다.[${error.toString()}]")
        }

        override fun onAuthError(error: ErrorCode)
        {
            super.onAuthError(error)
        }

        override fun onFailure(error: ErrorCode)
        {
            postErrorSnackBar("알 수 없는 이유로 등록에 실패했습니다.[${error.toString()}]")
        }
    }
}