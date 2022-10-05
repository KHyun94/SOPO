package com.delivery.sopo.presentation.register.viewmodel

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
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.BottomNotificationBar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfirmParcelViewModel @Inject constructor(
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
    private fun requestParcelRegister(register: Parcel.Register) = scope.launch {
        SopoLog.i("requestParcelRegister(...) 호출[${register.toString()}]")

        emitStatus(Result.Loading)
        parcel = registerParcelUseCase(register)

        delay(2000)

        emitStatus(Result.Success(parcel))
    }

    override fun handlerAPIException(exception: SOPOApiException)
    {
        super.handlerAPIException(exception)
        when(exception.code)
        {
            ErrorCode.ALREADY_REGISTERED_PARCEL, ErrorCode.OVER_REGISTERED_PARCEL, ErrorCode.PARCEL_BAD_REQUEST -> postErrorSnackBar(exception.message)
            ErrorCode.VALIDATION -> postErrorSnackBar(exception.message)
            else ->
            {
                exception.printStackTrace()
                postErrorSnackBar("[불명]${exception.message}")
            }
        }
    }
    override fun handlerInternalServerException(exception: InternalServerException)
    {
        super.handlerInternalServerException(exception)

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

    override fun handlerException(exception: Exception)
    {
        super.handlerException(exception)
        postErrorSnackBar("[불명] ${exception.toString()}")
    }

}