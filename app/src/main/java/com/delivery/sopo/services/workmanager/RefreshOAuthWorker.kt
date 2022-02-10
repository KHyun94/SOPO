package com.delivery.sopo.services.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.coroutineScope
import org.koin.core.KoinComponent
import org.koin.core.inject


class RefreshOAuthWorker(val context: Context, private val params: WorkerParameters):
        CoroutineWorker(context, params),
        KoinComponent
{
    private val oAuthRepo: OAuthLocalRepository by inject()

    init
    {
        SopoLog.i("RefreshOAuthWorker 실행")
    }

    override suspend fun doWork(): Result = coroutineScope {

        SopoLog.d(msg = "doWork() call")

        //        when(val result = OAuthCall.requestRefreshTokenInOAuth()){
        //            is NetworkResult.Success ->
        //            {
        //                val gson = Gson()
        //                val type = object : TypeToken<OAuthToken>() {}.type
        //                val reader = gson.toJson(result.data)
        //                val oauthResult = gson.fromJson<OAuthToken>(reader, type)
        //
        //                SopoLog.d("refresh OAuth 성공 [data:$oauthResult]")
        //
        //                withContext(Dispatchers.Default) {
        //                    val entity = OAuthMapper.objectToEntity(oauthResult)
        //                    oAuthRepo.update(entity)
        //                    Result.success()
        //                }
        //            }
        //            is NetworkResult.Error ->
        //            {
        //                val e = result.exception as APIException
        //                SopoLog.e("refresh OAuth 실패[error:$e]", e)
        //                Result.failure()
        //            }
        //        }

        Result.success()

    }
}