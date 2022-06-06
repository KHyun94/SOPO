package com.delivery.sopo.presentation.services.workmanager

import android.content.Context
import androidx.work.*
import com.delivery.sopo.consts.BundleConst
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import org.koin.core.KoinComponent


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

    suspend fun registerParcelWorkManager(context: Context, parcelRegister: Parcel.Register)
    {
        SopoLog.i(msg = "registerParcelWorkManager() 호출 [data:${parcelRegister.toString()}]")

        val workManager = WorkManager.getInstance(context)

        val jsonStr = Gson().toJson(parcelRegister)

        val inputData: Data = Data.Builder().putString(BundleConst.PARCEL_REGISTER_INFO, jsonStr).build()

        // work 인스턴스화
        val workRequest = OneTimeWorkRequestBuilder<RegisterParcelWorker>().setInputData(inputData).build()

        //work manager 등록
        workManager.enqueue(workRequest)
    }

    fun deleteParcelWorkManager(context: Context)
    {
        SopoLog.i(msg = "deleteParcelWorkManager() 호출")

        val workManager = WorkManager.getInstance(context)

        // work 인스턴스화
        val workRequest = OneTimeWorkRequestBuilder<DeleteParcelWorker>().build()

        // work UUID
        val workUUID = workRequest.id

        //work manager 등록
        workManager.enqueue(workRequest)
    }
}
