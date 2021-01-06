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
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject


class SOPOWorker(val context: Context, private val params: WorkerParameters) :
    CoroutineWorker(context, params), KoinComponent
{
    private val appDatabase: AppDatabase by inject()
    private val userRepoImpl: UserRepoImpl by inject()
    private val parcelRepoImpl: ParcelRepoImpl by inject()

    private val TAG = "SopoWorker"

    // Room 내 등록된 택배(진행 중)의 갯수 > 0 == true != false
    private suspend fun isEnrolledParcel(): Boolean
    {
        val cnt = parcelRepoImpl.getOnGoingDataCnt() ?: 0
        SopoLog.d(tag = "$TAG.isEnrolledParcel", msg = "등록된 소포의 갯수 => $cnt")
        return cnt > 0
    }

    // Room 내 저장된 택배들을 서버로 조회 요청
    private suspend fun patchParcels(): APIResult<String?>?
    {
        // 유저 이메일
        val email = userRepoImpl.getEmail()

        SopoLog.d(tag = "$TAG.patchParcels", msg = "유저 이메일 ===> $email")

        if (isEnrolledParcel())
        {
            SopoLog.d(tag = "$TAG.patchParcels", msg = "is Enrolled Parcel => YES")

            return NetworkManager.privateRetro.create(ParcelAPI::class.java)
                .parcelsRefreshing(email = email)
        }
        else
        {
            // 조회 안함 및 period worker 제거
            SopoLog.d(tag = "$TAG.patchParcels", msg = "is Enrolled Parcel => NO")
            return null
        }
    }


    // worker의 실행 메서드
   override suspend fun doWork(): Result = coroutineScope {
        withContext(Dispatchers.Default) {

//            val cnt = appDatabase.workDao().getCnt()
//            if (cnt == null || cnt == 0)
//            {
//                Result.success()
////                Result.failure()
//            }
//            else
//            {
//
//            }
            val result = patchParcels()

            if (result != null)
            {
                SopoLog.d(tag= "$TAG.doWork", msg = "Work Service => $result")

                if (result.code == "0000")
                {
                    SopoLog.d(tag = "$TAG.doWork", msg = "Success to build Worker  $result")

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
                                msg = "Failure to PATCH work manager, Because Of ErrorCode $result",
                                uuid = "1",
                                regDt = TimeUtil.getDateTime()
                            )
                        )


                    SopoLog.d(tag = TAG, msg = "Fail to build Worker  $result")
                    Result.failure()
                }
            }
            else
            {
                appDatabase.logDao()
                    .insert(
                        LogEntity(
                            no = 0,
                            msg = "Connect Fail to PATCH work manager becuase result null",
                            uuid = "1",
                            regDt = TimeUtil.getDateTime()
                        )
                    )


                Log.i(TAG, "Work Service Fail")

                Result.failure()
            }

        }
    }


}