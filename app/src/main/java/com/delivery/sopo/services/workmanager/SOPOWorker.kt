package com.delivery.sopo.services.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.database.room.entity.LogEntity
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject


class SOPOWorker(val context: Context, private val params: WorkerParameters) :
    CoroutineWorker(context, params), KoinComponent
{
    val appDatabase: AppDatabase by inject()

    private val TAG = "LOG.SOPO"

    private val userRepoImpl: UserRepoImpl by inject()
    private val parcelRepoImpl: ParcelRepoImpl by inject()

    suspend fun patchParcels(): APIResult<String?>?
    {
        val email = userRepoImpl.getEmail()

        if (isEnrolledParcel())
        {
            Log.d(TAG, "Enabled Work Manager")
            Log.d(TAG, "is Enrolled Parcel => YES")

            return NetworkManager.privateRetro.create(ParcelAPI::class.java)
                .parcelsRefreshing(email = email)
        }
        else
        {
            // 조회 안함 및 period worker 제거
            Log.d(TAG, "Disenabled Work Manager")
            Log.d(TAG, "is Enrolled Parcel => NO")
            return null
        }
    }

    private suspend fun isEnrolledParcel(): Boolean
    {
        val cnt = parcelRepoImpl.getOnGoingDataCnt() ?: 0
        Log.d(TAG, "등록된 소포의 갯수 => $cnt")
        return cnt > 0
    }

    override suspend fun doWork(): Result = coroutineScope {
        withContext(Dispatchers.Default) {

            val cnt = appDatabase.workDao().getCnt()

            if (cnt == null || cnt == 0)
            {
                Result.success()
//                Result.failure()
            }
            else
            {
                val result = patchParcels()

                if (result != null)
                {
                    Log.i(TAG, "Work Service => $result")

                    if (result.code == "0000")
                    {
                        Log.d(TAG, "Success to build Worker  $result")

                        appDatabase.logDao()
                            .insert(
                                LogEntity(
                                    no = 0,
                                    msg = "Success to PATCH work manager",
                                    uuid = "1",
                                    regDt = TimeUtil.getDateTime()
                                )
                            )

                        Result.success()
                    }
                    else
                    {
                        appDatabase.logDao()
                            .insert(
                                LogEntity(
                                    no = 0,
                                    msg = "Failure to PATCH work manager",
                                    uuid = "1",
                                    regDt = TimeUtil.getDateTime()
                                )
                            )


                        Log.d(TAG, "Fail to build Worker  $result")
                        Result.failure()
                    }
                }
                else
                {
                    appDatabase.logDao()
                        .insert(
                            LogEntity(
                                no = 0,
                                msg = "Connect Fail to PATCH work manager",
                                uuid = "1",
                                regDt = TimeUtil.getDateTime()
                            )
                        )


                    Log.i(TAG, "Work Service Fail")

                    Result.success()
                }
            }


        }
    }


}