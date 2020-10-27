package com.delivery.sopo.viewmodels.registesrs

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.models.ValidateResult
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.livedates.SingleLiveEvent
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterStep3ViewModel(
    private val userRepoImpl: UserRepoImpl
) : ViewModel()
{
    var waybilNum = MutableLiveData<String>()
    var courier = MutableLiveData<CourierItem>()
    var alias = MutableLiveData<String>()

    var validate =
        SingleLiveEvent<ValidateResult<Any?>>()

    val isRevise = SingleLiveEvent<Boolean>()

    init
    {
        validate.value = ValidateResult(false, "", null, InfoConst.NON_SHOW)
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
            .postParcel(
                email = userRepoImpl.getEmail(),
                parcelAlias = alias.value ?: "Default",
                trackCompany = courier.value!!.courierCode,
                trackNum = waybilNum.value!!
            )
            .enqueue(object : Callback<APIResult<ParcelId?>>
            {
                override fun onFailure(call: Call<APIResult<ParcelId?>>, t: Throwable)
                {
                    validate.value = ValidateResult(false, t.localizedMessage, t, InfoConst.NON_SHOW)
                }

                override fun onResponse(
                    call: Call<APIResult<ParcelId?>>,
                    response: Response<APIResult<ParcelId?>>
                )
                {
                    val httpStatusCode = response.code()

                    Log.d("LOG.SOPO", "뭐지? $httpStatusCode")
                    Log.d("LOG.SOPO", "뭐지? ${courier.value}")
                    Log.d("LOG.SOPO", "뭐지? ${waybilNum.value}")

                    // http status code 200
                    when (httpStatusCode)
                    {
                        201 ->
                        {
                            // 0000 =>
                            // 정상 조회 및 등록 성공
                            // 이상한 택배도 전부 등록
                            val result = response.body() as APIResult<ParcelId?>
                            validate.value = ValidateResult(true, "", null, InfoConst.NON_SHOW)
                            Log.d("LOG.SOPO", "등록 성공!!!!")
                        }
                        400 ->
                        {
                            // PC07 =>
                            // 이미 등록된 택배(서버 DB 상 택배 운송장 번호가 존재하면 나오는 에러 코드;택배사가 달라도 해당 에러코드 나오니 수정바람)
                            // 택배사만 잘못되도 해당 에러 코드 발생
                            val errorReader = response.errorBody()!!.charStream()

                            val result = Gson().fromJson(errorReader, APIResult::class.java)
                            Log.e("LOG.SOPO", "[ERROR] getParcels => $result ")


                            val msg = if (result == null)
                            {
                                Log.d("LOG.SOPO", "등록 null")
                                "알 수 없는 에러"
                            }
                            else
                            {
                                Log.d("LOG.SOPO", "등록 Code ${result.code}")
                                CodeUtil.returnCodeMsg(result.code)
                            }

                            validate.value = ValidateResult(false, msg, null, InfoConst.CUSTOM_DIALOG)
                        }
                        else ->
                        {
                            validate.value = ValidateResult(false, "알 수 없는 에러", null, InfoConst.CUSTOM_DIALOG)
                            Log.d("LOG.SOPO", "알 수 없는 에러!!!!")
                        }
                    }
                }
            })
    }
}