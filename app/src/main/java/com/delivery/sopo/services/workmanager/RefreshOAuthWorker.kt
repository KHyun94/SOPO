 package com.delivery.sopo.services.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.delivery.sopo.BuildConfig
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.data.repository.database.room.AppDatabase
import com.delivery.sopo.data.repository.database.room.entity.LogEntity
import com.delivery.sopo.data.repository.local.o_auth.OAuthEntity
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.call.ParcelCall
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.o_auth.OAuthRemoteRepository
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.TestResult
import com.delivery.sopo.models.dto.OAuthDTO
import com.delivery.sopo.models.mapper.OAuthMapper
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


class RefreshOAuthWorker(val context: Context, private val params: WorkerParameters): CoroutineWorker(context, params), KoinComponent
{
    private val appDatabase: AppDatabase by inject()
    private val userRepo: UserLocalRepository by inject()
    private val oAuthRepo: OAuthLocalRepository by inject()
    private val parcelRepository: ParcelRepository by inject()


    private suspend fun requestRefreshOAuthToken()
    {

    }

    override suspend fun doWork(): Result = coroutineScope {

        SopoLog.d( msg = "doWork() call")

        when(val result = UserCall.requestRefreshTokenInOAuth()){
            is NetworkResult.Success ->
            {
                val gson = Gson()
                val type = object : TypeToken<OAuthDTO>() {}.type
                val reader = gson.toJson(result.data)
                val oauthResult = gson.fromJson<OAuthDTO>(reader, type)

                SopoLog.d("refresh OAuth 성공 [data:$oauthResult]")

                withContext(Dispatchers.Default) {
                    val entity = OAuthMapper.objectToEntity(oauthResult)
                    oAuthRepo.update(entity)

                    Result.success()
                }

            }
            is NetworkResult.Error ->
            {
                val e = result.exception as APIException
                SopoLog.e("refresh OAuth 실패[error:$e]", e)
                Result.failure()
            }
        }
    }
}