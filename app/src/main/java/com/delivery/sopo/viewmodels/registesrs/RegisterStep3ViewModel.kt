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
    var wayBilNum = MutableLiveData<String>()
    var courier = MutableLiveData<CourierItem>()
    var alias = MutableLiveData<String>()

    val isRevise = SingleLiveEvent<Boolean>()

    private var _isProgress = MutableLiveData<Boolean>().apply { value = false }
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
        _isProgress.value = true

        check(wayBilNum.value.isGreaterThanOrEqual(9)){
            _result.postValue(TestResult.ErrorResult<String>(errorMsg = "송장 번호를 다시 확인해주세요.", errorType = ErrorResult.ERROR_TYPE_DIALOG))
            _isProgress.postValue(false)
        }

        check(courier.value != null){
            _result.postValue(TestResult.ErrorResult<String>(errorMsg = "택배사를 다시 확인해주세요.", errorType = ErrorResult.ERROR_TYPE_DIALOG))
            _isProgress.postValue(false)
        }

        CoroutineScope(Dispatchers.IO).launch {
            when(val result = ParcelCall.registerParcel(parcelAlias = alias.value ?: wayBilNum.value!!, trackCompany = courier.value!!.courierCode, trackNum = wayBilNum.value!!))
            {
                is NetworkResult.Success ->
                {
                   val data = result.data
                    val code = CodeUtil.getCode(data.code)

                    _result.postValue(TestResult.SuccessResult<ParcelId?>(code, code.MSG, data.data))
                    _isProgress.postValue(false)
                }
                is NetworkResult.Error ->
                {
                    val error = result.exception as APIException
                    val code = CodeUtil.getCode(error.data()?.code)

                    _result.postValue(TestResult.ErrorResult<String>(code, code.MSG, ErrorResult.ERROR_TYPE_DIALOG, null, error))
                    _isProgress.postValue(false)
                }
            }
        }
    }
}