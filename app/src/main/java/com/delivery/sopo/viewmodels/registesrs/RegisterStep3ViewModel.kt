package com.delivery.sopo.viewmodels.registesrs

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.extensions.isGreaterThanOrEqual
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.TestResult
import com.delivery.sopo.models.ValidateResult
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI
import com.delivery.sopo.networks.call.ParcelCall
import com.delivery.sopo.networks.dto.parcels.RegisterParcelDTO
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.livedates.SingleLiveEvent
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterStep3ViewModel(
    private val userRepoImpl: UserRepoImpl
) : ViewModel()
{
    var waybillNum = MutableLiveData<String>()
    var courier = MutableLiveData<CourierItem>()
    var alias = MutableLiveData<String>()

    val isRevise = SingleLiveEvent<Boolean>()

    private var _isProgress = MutableLiveData<Boolean>()
    val isProgress : LiveData<Boolean>
    get() = _isProgress

    private var _result = MutableLiveData<TestResult>()
    val result : LiveData<TestResult>
        get() = _result

    fun onReviseClicked()
    {
        isRevise.value = true
    }

    // '등록하기' Button Click event
    fun onRegisterClicked()
    {
        // TODO 각 값 유효성 검사 필요
        val registerParcelDTO = RegisterParcelDTO(courier.value!!.courierCode, waybillNum.value.toString(), alias.value.toString())

        CoroutineScope(Dispatchers.IO).launch {
            when(val result = ParcelCall.registerParcel(registerParcelDTO))
            {
                is NetworkResult.Success ->
                {
                   val data = result.data
                    val code = CodeUtil.getCode(data.code)

                    _result.postValue(TestResult.SuccessResult<ParcelId?>(code, code.MSG, data.data))
//                    _isProgress.postValue(false)
                }
                is NetworkResult.Error ->
                {
                    val exception = result.exception as APIException
                    val code = exception.responseCode

                    _result.postValue(TestResult.ErrorResult<String>(code, code.MSG, ErrorResult.ERROR_TYPE_DIALOG, null, exception))
                }
            }
        }
    }
}