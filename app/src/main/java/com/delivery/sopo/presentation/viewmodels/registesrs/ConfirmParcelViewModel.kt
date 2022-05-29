package com.delivery.sopo.presentation.viewmodels.registesrs

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.domain.usecase.parcel.remote.RegisterParcelUseCase
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.SopoLog
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
    val navigator: LiveData<String>
        get() = _navigator

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
    private fun requestParcelRegister(register: Parcel.Register) =
        scope.launch(coroutineExceptionHandler) {
            SopoLog.i("requestParcelRegister(...) 호출[${register.toString()}]")

            try
            {
                onStartLoading()
                parcel = registerParcelUseCase(register)
                _navigator.postValue(NavigatorConst.REGISTER_SUCCESS)
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