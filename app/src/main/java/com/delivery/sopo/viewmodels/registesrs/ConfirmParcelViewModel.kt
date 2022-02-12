package com.delivery.sopo.viewmodels.registesrs

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.ParcelExceptionHandler
import com.delivery.sopo.R
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.NavigatorEnum
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.*
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConfirmParcelViewModel(private val parcelRepo: ParcelRepository): BaseViewModel()
{
    val waybillNum = MutableLiveData<String>()
    val carrier = MutableLiveData<Carrier>()
    val alias = MutableLiveData<String?>()

    private var _navigator = MutableLiveData<NavigatorEnum?>()
    val navigator: LiveData<NavigatorEnum?>
        get() = _navigator

    fun onMoveToNav(v: View) = checkEventStatus(checkNetwork = true) {
        when(v.id)
        {
            R.id.tv_revise ->
            {
                _navigator.value = NavigatorEnum.REGISTER_INPUT_REVISE
            }
            R.id.tv_init ->
            {
                _navigator.value = NavigatorEnum.REGISTER_INPUT_INIT
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
    private fun requestParcelRegister(register: Parcel.Register) = scope.launch(Dispatchers.IO) {
        SopoLog.i("requestParcelRegister(...) 호출[${register.toString()}]")

        try
        {
            parcelRepo.registerParcel(register).apply {
                SopoLog.d("택배 등록 성공 [번호:$this]")
            }

            _navigator.postValue(NavigatorEnum.REGISTER_INPUT_SUCCESS)
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
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

            postErrorSnackBar("로그인이 실패했습니다. 다시 시도해주세요.[${error.toString()}]")
        }
    }

    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        ParcelExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }
}