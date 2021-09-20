package com.delivery.sopo.services.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.delivery.sopo.data.repository.remote.parcel.ParcelUseCase
import com.delivery.sopo.models.ParcelRegisterDTO
import com.delivery.sopo.notification.NotificationImpl
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.koin.core.KoinComponent


class RegisterParcelWorker(val context: Context, private val params: WorkerParameters): CoroutineWorker(context, params), KoinComponent
{
    init
    {
        SopoLog.i("RegisterParcelWorker 실행")
    }

    private fun changeJsonToObj(json:String):ParcelRegisterDTO = Gson().fromJson(json, ParcelRegisterDTO::class.java)

    override suspend fun doWork(): Result = coroutineScope {

        SopoLog.i(msg = "doWork() 호출")

        SopoLog.d("requestParcelRegister(...) 호출")

        val registerDTO = inputData.getString("RECEIVED_STR")?.let {str ->
            changeJsonToObj(str)
        } ?: return@coroutineScope Result.failure()

        val res = ParcelUseCase.requestParcelRegister(registerDTO = registerDTO)

        if(!res.result)
        {
            SopoLog.e("work - requestParcelRegister(...) 실패[code:${res.code}][message:${res.message}]")
            return@coroutineScope Result.failure()
        }

        if(res.data == null)
        {
            SopoLog.e("work - requestParcelRegister(...) 실패[code:${res.code}][message:${res.message}]")
            return@coroutineScope Result.failure()
        }

        SopoLog.d("work - requestParcelRegister(...) 성공[code:${res.code}][message:${res.message}]")

        NotificationImpl.alertRegisterParcel(context = context, registerDTO = registerDTO)

        return@coroutineScope Result.success()
    }
}