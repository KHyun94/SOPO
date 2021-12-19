package com.delivery.sopo.services.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.models.ParcelRegister
import com.delivery.sopo.notification.NotificationImpl
import com.delivery.sopo.usecase.parcel.remote.DeleteParcelsUseCase
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject


class DeleteParcelWorker(val context: Context, private val params: WorkerParameters): CoroutineWorker(context, params), KoinComponent
{
    init
    {
        SopoLog.i("DeleteParcelWorker 실행")
    }

    private val deleteParcelsUseCase: DeleteParcelsUseCase by inject()

    override suspend fun doWork(): Result = coroutineScope {

        SopoLog.i(msg = "doWork() 호출")

        SopoLog.i(msg = "1")
        delay(5000)
        SopoLog.i(msg = "2")
        deleteParcelsUseCase.invoke()
        SopoLog.i(msg = "3")
        return@coroutineScope Result.success()
    }
}