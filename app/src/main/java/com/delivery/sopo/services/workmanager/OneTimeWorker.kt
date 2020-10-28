package com.delivery.sopo.services.workmanager

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OneTimeWorker(val context: Context, params: WorkerParameters) : CoroutineWorker(
    context,
    params
)
{
    val TAG = "LOG.SOPO"

    override suspend fun doWork(): Result
    {
        val a = mutableListOf<Int>(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        Log.d(TAG, "Period Time Worker Service Start!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Main)
            {
                Toast.makeText(
                    context,
                    "Period Time Worker Service Start!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!",
                    Toast.LENGTH_LONG
                ).show()

            }
        }

        return Result.success()
    }
}