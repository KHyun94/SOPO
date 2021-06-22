package com.delivery.sopo.viewmodels.registesrs

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.data.repository.remote.parcel.ParcelRemoteRepository
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.CarrierDTO
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.ParcelRegisterDTO
import com.delivery.sopo.models.TestResult
import com.delivery.sopo.networks.call.ParcelCall
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.views.registers.RegisterMainFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConfirmParcelViewModel: ViewModel()
{
    var waybillNum = MutableLiveData<String>()
    var carrier = MutableLiveData<CarrierDTO>()
    var alias = MutableLiveData<String>()

    private var _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    private var _isProgress = MutableLiveData<Boolean>()
    val isProgress: LiveData<Boolean>
        get() = _isProgress

    private var _result = MutableLiveData<TestResult>()
    val result: LiveData<TestResult>
        get() = _result


    fun onMoveFirstStep(v: View)
    {
        when(v.id)
        {
            R.id.tv_revise ->
            {
                _navigator.value = RegisterMainFragment.REGISTER_REVISE
            }
            R.id.tv_init ->
            {
                _navigator.value = RegisterMainFragment.REGISTER_INIT
            }
            R.id.tv_register ->
            {
                requestParcelRegister()
            }
        }
    }

    // '등록하기' Button Click event
    private fun requestParcelRegister()
    {
        val registerDTO = ParcelRegisterDTO(carrier = carrier.value?.carrier?:throw Exception("Carrier must be not null"),
                                            waybillNum = waybillNum.value.toString(),
                                            alias = alias.value.toString())

        CoroutineScope(Dispatchers.IO).launch {

            val res = ParcelRemoteRepository.requestParcelRegister(registerDTO)



            when(val result = ParcelCall.registerParcel(registerDTO))
            {
                is NetworkResult.Success ->
                {
                    val data = result.data
                    val code = CodeUtil.getCode(data.code)

                    _result.postValue(
                        TestResult.SuccessResult<Int?>(code, code.MSG, data.data))
                    //                    _isProgress.postValue(false)
                }
                is NetworkResult.Error ->
                {
                    val exception = result.exception as APIException
                    val code = exception.responseCode

                    _result.postValue(TestResult.ErrorResult<String>(code, code.MSG,
                                                                     ErrorResult.ERROR_TYPE_DIALOG,
                                                                     null, exception))
                }
            }
        }
    }
}