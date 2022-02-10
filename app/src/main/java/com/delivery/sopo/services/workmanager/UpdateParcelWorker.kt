package com.delivery.sopo.services.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.room.entity.LogEntity
import com.delivery.sopo.usecase.parcel.remote.RefreshParcelsUseCase
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.coroutineScope
import org.koin.core.KoinComponent
import org.koin.core.inject

class UpdateParcelWorker(val context: Context, private val params: WorkerParameters): CoroutineWorker(context, params), KoinComponent
{
    // TODO 워크매니저에서 해당 레포들을 파라미터 형식으로 처리하는 방법을 고민
    private val refreshParcelsUseCase: RefreshParcelsUseCase by inject()
    private val appDatabase: AppDatabase by inject()

    override suspend fun doWork(): Result = coroutineScope {

        SopoLog.d(msg = "doWork() call")

        try
        {
            refreshParcelsUseCase.invoke()
            appDatabase.logDao().insert(LogEntity(no = 0, msg = "Success to UpdateParcelWorker", uuid = "1", regDt = TimeUtil.getDateTime()))
            Result.success()
        }
        catch(e: Exception)
        {
            SopoLog.e(msg = "Fail to PATCH work manager.[message:${e.message}]", e)
            appDatabase.logDao().insert(LogEntity(no = 0, msg = "Failure to UpdateParcelWorker [message:${e.message}]", uuid = "1", regDt = TimeUtil.getDateTime()))
            Result.failure()
        }

    }
}