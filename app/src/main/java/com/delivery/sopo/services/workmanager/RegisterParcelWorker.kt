package com.delivery.sopo.services.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.delivery.sopo.BuildConfig
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.database.room.AppDatabase
import com.delivery.sopo.data.repository.database.room.entity.LogEntity
import com.delivery.sopo.data.repository.local.o_auth.OAuthEntity
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.call.ParcelCall
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.o_auth.OAuthRemoteRepository
import com.delivery.sopo.data.repository.remote.parcel.ParcelUseCase
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.ParcelRegisterDTO
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.TestResult
import com.delivery.sopo.models.dto.OAuthDTO
import com.delivery.sopo.models.mapper.OAuthMapper
import com.delivery.sopo.models.push.UpdatedParcelInfo
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.OAuthAPI
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.TimeUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject


class RegisterParcelWorker(val context: Context, private val params: WorkerParameters): CoroutineWorker(context, params), KoinComponent
{
    init
    {
        SopoLog.i("RegisterParcelWorker 실행")
    }

    private fun changeJsonToObj(json:String):ParcelRegisterDTO{
        return Gson().fromJson(json, ParcelRegisterDTO::class.java)
    }

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

        SopoLog.e("work - requestParcelRegister(...) 성공[code:${res.code}][message:${res.message}]")

        return@coroutineScope Result.success()
    }
}