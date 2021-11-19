package com.delivery.sopo.services.workmanager

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.delivery.sopo.data.repository.database.room.AppDatabase
import com.delivery.sopo.models.ParcelRegister
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import java.util.concurrent.TimeUnit


object SOPOWorkManager: KoinComponent
{
    fun updateWorkManager(context: Context)
    {
        SopoLog.d(msg = "updateWorkManager() 호출")

        val workManager = WorkManager.getInstance(context)

        val workRequest = OneTimeWorkRequestBuilder<UpdateParcelWorker>().build()

        SopoLog.d(msg = "Register New Worker Start!!!")

        //work manager 등록
        workManager.enqueue(workRequest)
    }

    suspend fun registerParcelWorkManager(context: Context, registerParcelRegister: ParcelRegister)
    {
        SopoLog.i(msg = "registerParcelWorkManager() 호출 [data:${registerParcelRegister.toString()}]")

        val workManager = WorkManager.getInstance(context)

        val jsonStr = Gson().toJson(registerParcelRegister)

        val inputData = Data.Builder().putString("RECEIVED_STR", jsonStr).build()

        // work 인스턴스화
        val workRequest =
            OneTimeWorkRequestBuilder<RegisterParcelWorker>().setInputData(inputData).build()

        // work UUID
        val workUUID = workRequest.id

        //work manager 등록
        workManager.enqueue(workRequest)
    }
}
