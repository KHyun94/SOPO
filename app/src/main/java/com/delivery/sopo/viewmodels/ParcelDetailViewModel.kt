package com.delivery.sopo.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.StatusItem
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.ParcelAPI
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.shared.UserRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ParcelDetailViewModel(
    private val userRepo: UserRepo,
    private val parcelRepoImpl: ParcelRepoImpl
) : ViewModel()
{
    val TAG = "LOG.SOPO"

    var statusList = mutableListOf<StatusItem>(
        StatusItem("상품인수", false),
        StatusItem("배송중", true),
        StatusItem("동네도착", false),
        StatusItem("배송완료", false)
    )

    fun requestFetchParcel(parcelUId: String, regDt: String)
    {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Main){
                NetworkManager.privateRetro.create(ParcelAPI::class.java)
                    .requestRenewalOneParcel(
                        email = "gnltlgnlrl94@naver.com",
                        parcelUid = parcelUId,
                        regDt = regDt
                    ).enqueue(object : Callback<APIResult<Parcel?>>
                    {
                        override fun onFailure(call: Call<APIResult<Parcel?>>, t: Throwable)
                        {
                            Log.d(TAG, "에러 ${t.message}")
                        }

                        override fun onResponse(
                            call: Call<APIResult<Parcel?>>,
                            response: Response<APIResult<Parcel?>>
                        )
                        {
                            when
                            {
                                response.code() == 200 -> Log.d(TAG, "정상 ${response.body()}")
                                response.code() == 400 -> Log.d(TAG, "정상 ${response.errorBody()}")
                                else -> Log.d(TAG, "에러러러")
                            }
                        }

                    })

            }
        }

//            .requestRenewalOneParcel(email = userRepo.getEmail(), parcelUid = parcelUId, regDt = regDt)
    }
}