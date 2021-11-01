package com.delivery.sopo.services.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.data.repository.remote.parcel.ParcelUseCase
import com.delivery.sopo.models.ParcelRegisterDTO
import com.delivery.sopo.notification.NotificationImpl
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject


class RegisterParcelWorker(val context: Context, private val params: WorkerParameters): CoroutineWorker(context, params), KoinComponent
{
    init
    {
        SopoLog.i("RegisterParcelWorker 실행")
    }

    private val parcelRepo: ParcelRepository by inject()

    private fun changeJsonToObj(json:String):ParcelRegisterDTO = Gson().fromJson(json, ParcelRegisterDTO::class.java)

    override suspend fun doWork(): Result = coroutineScope {

        SopoLog.i(msg = "doWork() 호출")

        SopoLog.d("requestParcelRegister(...) 호출")

        // TODO 에러 처리

        val registerDTO = inputData.getString("RECEIVED_STR")?.let {str ->
            changeJsonToObj(str)
        } ?: return@coroutineScope Result.failure()

        val res = parcelRepo.registerParcel(parcel = registerDTO)

        NotificationImpl.alertRegisterParcel(context = context, registerDTO = registerDTO)

        return@coroutineScope Result.success()
    }
}