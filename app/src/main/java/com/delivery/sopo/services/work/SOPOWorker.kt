package com.delivery.sopo.services.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.delivery.sopo.models.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.ParcelAPI
import com.delivery.sopo.repository.ParcelRepoImpl
import com.delivery.sopo.repository.shared.UserRepo
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SOPOWorker(context: Context, private val params: WorkerParameters) :
    CoroutineWorker(context, params), KoinComponent
{
    private val TAG = "LOG.SOPO"

    private val userRepo: UserRepo by inject()

    private val parcelRepoImpl: ParcelRepoImpl by inject()

    suspend fun requestRenewal()
    {
        val email = userRepo.getEmail()

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
                    }

                    override fun onResponse(
                        call: Call<APIResult<String?>>,
                        response: Response<APIResult<String?>>
                    )
                    {
                        val httpStatusCode = response.code()

                        when (httpStatusCode)
                        {
                            200 ->
                            {
                                // 성공적인 조회
                            }
                            400 ->
                            {

                            }
                            else ->
                            {
                                // 조회 실패

                            }
                        }

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
        return cnt < 0
    }


    override suspend fun doWork(): Result
    {


        return Result.success()
    }

}