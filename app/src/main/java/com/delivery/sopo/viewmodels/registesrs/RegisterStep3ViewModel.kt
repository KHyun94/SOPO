package com.delivery.sopo.viewmodels.registesrs

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.ParcelAPI
import com.delivery.sopo.repository.UserRepo
import com.delivery.sopo.util.fun_util.CodeUtil
import com.delivery.sopo.util.fun_util.SingleLiveEvent
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
        NetworkManager.privateRetro
            .create(ParcelAPI::class.java)
            .registerParcel(
                email = userRepo.getEmail(),
                parcelAlias = alias.value,
                trackCompany = courier.value!!.courierCode,
//                trackCompany = "에러일으킬 용도!!!",
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
                            // if it work completely, we should pare in response header - location(api end point)which recently registered parcel date
                            val headers = response.headers()

                            for(head in headers)
                            {
                                Log.d("LOG.SOPO", "Header ${head}")
                            }

                        }
                        else ->
                        {
                            Log.d("LOG.SOPO", "왓이즈 코드 ${result!!.code}")

                            errorMsg.value = CodeUtil.returnCodeMsg(result!!.code) ?: "널이야"
                        }
                    }
                }
            })
    }
}