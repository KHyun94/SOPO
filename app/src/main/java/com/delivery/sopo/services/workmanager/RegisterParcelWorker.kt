package com.delivery.sopo.services.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.delivery.sopo.consts.BundleConst
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.models.ParcelRegister
import com.delivery.sopo.notification.NotificationImpl
import com.delivery.sopo.usecase.parcel.remote.GetParcelUseCase
import com.delivery.sopo.usecase.parcel.remote.RegisterParcelUseCase
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject


class RegisterParcelWorker(val context: Context, private val params: WorkerParameters): CoroutineWorker(context, params), KoinComponent
{
    private val registerParcelUseCase: RegisterParcelUseCase by inject()
    private val getParcelUseCase: GetParcelUseCase by inject()

    init
    {
        SopoLog.i("RegisterParcelWorker 실행")
    }

    private fun changeJsonToObj(json:String):ParcelRegister = Gson().fromJson(json, ParcelRegister::class.java)

    override suspend fun doWork(): Result = coroutineScope {

        SopoLog.i(msg = "doWork() 호출")

        SopoLog.d("requestParcelRegister(...) 호출")

        val parcelRegister = inputData.getString(BundleConst.PARCEL_REGISTER_INFO)?.let { json ->
            Gson().fromJson(json, ParcelRegister::class.java)
        } ?: return@coroutineScope Result.failure()

        try
        {
            val parcelId = registerParcelUseCase.invoke(parcelRegister = parcelRegister)
            val parcel = getParcelUseCase.invoke(parcelId = parcelId)

//            NotificationImpl.notifyRegisterParcel(context = context, parcel = parcel)

            return@coroutineScope Result.success()
        }
        catch(exception: Exception)
        {
            when(exception)
            {
                is SOPOApiException ->
                {
                    val errorCode = ErrorEnum.getErrorCode(exception.getErrorResponse().code)
                    SopoLog.e("SOPO API Error $errorCode", exception)

                    if(errorCode != ErrorEnum.ALREADY_REGISTERED_PARCEL) return@coroutineScope Result.failure()

                    val data: Data = Data.Builder().putString(BundleConst.DO_WORK_RESULT, BundleConst.ERROR_ALREADY_REGISTERED_PARCEL).build()

                    return@coroutineScope Result.failure(data)
                }
                else ->
                {
                    return@coroutineScope Result.failure()
                }
            }
        }
    }
}