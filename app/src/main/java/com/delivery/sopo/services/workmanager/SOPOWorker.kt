 package com.delivery.sopo.services.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.database.room.entity.LogEntity
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.call.ParcelCall
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.coroutineScope
import org.koin.core.KoinComponent
import org.koin.core.inject


class SOPOWorker(val context: Context, private val params: WorkerParameters): CoroutineWorker(context, params), KoinComponent
{
    private val TAG = this.javaClass.simpleName

    private val appDatabase: AppDatabase by inject()
    private val parcelRepoImpl: ParcelRepoImpl by inject()

    // Room 내 등록된 택배(진행 중)의 갯수 > 0 == true != false
    private suspend fun isEnrolledParcel(): Boolean
    {
        val cnt = parcelRepoImpl.getOnGoingDataCnt()
        return cnt > 0
    }

    // Room 내 저장된 택배들을 서버로 조회 요청
    private suspend fun requestRefreshParcel(): NetworkResult<APIResult<String?>>?
    {
        return when (isEnrolledParcel())
        {
            true -> ParcelCall.requestRefreshParcels()
            false -> null
        }
    }

    override suspend fun doWork(): Result = coroutineScope {

        SopoLog.d( msg = "doWork() call")

        when (val result = requestRefreshParcel())
        {
            is NetworkResult.Success ->
            {
                SopoLog.d( msg = "Success to PATCH work manager")
                appDatabase.logDao()
                    .insert(LogEntity(no = 0, msg = "Success to PATCH work manager", uuid = "1", regDt = TimeUtil.getDateTime()))
                Result.success()
            }
            is NetworkResult.Error ->
            {
                SopoLog.e( msg = "Fail to PATCH work manager. ${result.exception.message}")
                appDatabase.logDao()
                    .insert(LogEntity(no = 0, msg = "Failure to PATCH work manager, Because Of ErrorCode $result", uuid = "1", regDt = TimeUtil.getDateTime()))

                Result.failure()
            }
            else ->
            {
                SopoLog.e( msg = "Fail to PATCH work manager. Because of Result NULL")
                appDatabase.logDao()
                    .insert(LogEntity(no = 0, msg = "Failure to PATCH work manager, Because Of ErrorCode $result", uuid = "1", regDt = TimeUtil.getDateTime()))
                Result.failure()
            }
        }

    }
}