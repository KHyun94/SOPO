package com.delivery.sopo.presentation.services.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.delivery.sopo.util.SopoLog

class OneTimeWorker(val context: Context, params: WorkerParameters) : CoroutineWorker(
    context,
    params
)
{
    override suspend fun doWork(): Result
    {
        SopoLog.d(msg = "Period Time Worker Service Start!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

        return Result.success()
    }
}