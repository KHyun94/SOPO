package com.delivery.sopo.viewmodels.registesrs

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.ParcelAPI
import com.delivery.sopo.repository.shared.UserRepo
import com.delivery.sopo.util.fun_util.CodeUtil
import com.delivery.sopo.util.fun_util.SingleLiveEvent
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterStep3ViewModel(
    private val userRepo: UserRepo
) : ViewModel()
{
    var waybilNum = MutableLiveData<String>()
    var courier = MutableLiveData<CourierItem>()
    var alias = MutableLiveData<String>()

    var errorMsg = SingleLiveEvent<String?>()

    val isRevise = SingleLiveEvent<Boolean>()

    init
    {
        errorMsg.value = ""
    }

    fun onReviseClicked()
    {
        isRevise.value = true
    }

    // '등록하기' Button Click event
    fun onRegisterClicked()
    {

        viewModelScope.launch {
            NetworkManager.privateRetro
                .create(ParcelAPI::class.java)
                .registerParcel(
                    email = userRepo.getEmail(),
                    parcelAlias = alias.value?:"Default",
                    trackCompany = courier.value!!.courierCode ,
                    trackNum = waybilNum.value!!
                )
                .enqueue(object : Callback<APIResult<ParcelId?>>
                {
                    override fun onFailure(call: Call<APIResult<ParcelId?>>, t: Throwable)
                    {
                        TODO("Not yet implemented")
                    }

                    override fun onResponse(
                        call: Call<APIResult<ParcelId?>>,
                        response: Response<APIResult<ParcelId?>>
                    )
                    {
                        val httpStatusCode = response.code()

                        val result = response.body()

                        Log.d("LOG.SOPO", "뭐지? $httpStatusCode")
                        Log.d("LOG.SOPO", "뭐지? $result")
                        Log.d("LOG.SOPO", "뭐지? ${courier.value}")
                        Log.d("LOG.SOPO", "뭐지? ${waybilNum.value}")

                        // http status code 200
                        when (httpStatusCode)
                        {
                            201 ->
                            {
                                Log.d("LOG.SOPO", "등록 성공!!!!")
                            }
                            else ->
                            {
                                val msg = if(result == null)
                                {
                                    Log.d("LOG.SOPO", "등록 null")
                                    "결과 null"
                                }
                                else if(result.code == null)
                                {
                                    Log.d("LOG.SOPO", "등록 Code null")
                                    "코드 null"
                                }
                                else
                                {
                                    Log.d("LOG.SOPO", "등록 Code ${result.code}")
                                    CodeUtil.returnCodeMsg(result.code)
                                }

                                errorMsg.value = msg

                            }
                        }
                    }
                })
        }

    }
}