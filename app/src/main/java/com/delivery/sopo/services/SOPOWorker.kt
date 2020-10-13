package com.delivery.sopo.services

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SOPOWorker(context: Context, private val params: WorkerParameters) :
    CoroutineWorker(context, params), KoinComponent
{
    private val TAG = "LOG.SOPO"

    private val userRepoImpl : UserRepoImpl by inject()
    private val parcelRepoImpl: ParcelRepoImpl by inject()

    suspend fun requestRenewal()
    {
        val email = userRepoImpl.getEmail()

        if (isEnrolledParcel())
        {
            Log.d(TAG, "Enabled Work Manager")
            Log.d(TAG, "is Enrolled Parcel => YES")

            NetworkManager.privateRetro.create(ParcelAPI::class.java)
                .requestRenewal(email = email)
                .enqueue(object : Callback<APIResult<String?>>
                {
                    override fun onFailure(call: Call<APIResult<String?>>, t: Throwable)
                    {
                        Log.d("LOG.SOPO", "Fail!!!!!! BACK END!!!!!!!!!!!!!!!Service")
                    }

                    override fun onResponse(
                        call: Call<APIResult<String?>>,
                        response: Response<APIResult<String?>>
                    )
                    {
                        Log.d("LOG.SOPO", "BACK END!!!!!!!!!!!!!!!Service")
//                        val httpStatusCode = response.code()
//
//                        when (httpStatusCode)
//                        {
//                            200 ->
//                            {
//                                // 성공적인 조회
//                            }
//                            400 ->
//                            {
//
//                            }
//                            else ->
//                            {
//                                // 조회 실패
//
//                            }
//                        }

                    }
                })
        }
        else
        {
            // 조회 안함 및 period worker 제거
            Log.d(TAG, "Disenabled Work Manager")
            Log.d(TAG, "is Enrolled Parcel => NO")
        }
    }

    private suspend fun isEnrolledParcel(): Boolean
    {
        val cnt = parcelRepoImpl.getOnGoingDataCnt() ?: 0
        Log.d(TAG, "등록된 소포의 갯수 => $cnt")
        return cnt > 0
    }


    override suspend fun doWork(): Result
    {
        requestRenewal()
        return Result.success()
    }

}