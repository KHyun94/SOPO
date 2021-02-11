package com.delivery.sopo.services.workmanager

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.delivery.sopo.util.SopoLog

class OneTimeWorker(val context: Context, params: WorkerParameters) : CoroutineWorker(
    context,
    params
)
{
    val TAG = "LOG.SOPO"

    override suspend fun doWork(): Result
    {
        SopoLog.d(
            tag = TAG,
            msg = "Period Time Worker Service Start!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
        )


        return Result.success()
    }
}