package com.delivery.sopo.services.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class SOPOWorker(context: Context, private val params: WorkerParameters) : Worker(context, params)
{
    private fun requestRenewal()
    {
        
    }

    override fun doWork(): Result
    {
        return Result.failure()
    }
}