package com.delivery.sopo.networks

import com.delivery.sopo.SOPOApp
import com.delivery.sopo.database.room.entity.OauthEntity
import com.delivery.sopo.enums.ResponseCode.*
import com.delivery.sopo.mapper.OauthMapper
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.OauthResult
import com.delivery.sopo.models.TestResult
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.api.OAuthAPI
import com.delivery.sopo.repository.impl.OauthRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.koin.core.KoinComponent
import org.koin.core.inject

class TokenAuthenticator : Authenticator, KoinComponent
{
    val TAG = this.javaClass.simpleName
    val userRepoImpl : UserRepoImpl by inject()
    val oauthRepoImpl : OauthRepoImpl by inject()

    override fun authenticate(route : Route?, response : Response) : Request?
    {
        if (response.code == 401)
        {
            SopoLog.d( msg = "authenticate call() - 401")

            return when (val result = requestRefreshOAuthToken())
            {
                is TestResult.SuccessResult<*> ->
                {
                    val oauth = result.data as OauthResult
                    val accessToken = oauth.accessToken

                    SopoLog.d( msg = "authenticate success => ${accessToken}")

                    getRetrofitWithoutAuthenticator(response, accessToken)
                }
                is TestResult.ErrorResult<*> ->
                {
                    SopoLog.e( msg = "authenticate fail => ${result.errorMsg}")
                    null
                }
            }
        }

        SopoLog.d( msg = "authenticate call() - else")
        return null
    }

    private fun requestRefreshOAuthToken() : TestResult
    {
        var oauth : OauthEntity?

        // ROOM 내 저장된 OAuth 데이터 호출
        runBlocking {
            withContext(Dispatchers.Default) {
                oauth = oauthRepoImpl.get(userRepoImpl.getEmail())
            }
        }

        // OAuth가 null 일 때 Error 호출
        if (oauth == null) return TestResult.ErrorResult(LOCAL_ERROR_NULL_POINT, LOCAL_ERROR_NULL_POINT.MSG, ErrorResult.ERROR_TYPE_NON, null, null)

        val retro = NetworkManager.retro("sopo-aos", "sopoAndroid!!@@").create(OAuthAPI::class.java)
        val result = retro.requestRefreshOAuthToken(
            grantType = "refresh_token", email = userRepoImpl.getEmail(), refreshToken = oauth?.refreshToken
                ?: "", deviceInfo = SOPOApp.deviceInfo
        ).execute()

        if (!result.isSuccessful)
        {
            val errorBody = result.errorBody()
            val errorReader = errorBody?.charStream()
            val apiResult = Gson().fromJson(errorReader, APIResult::class.java)
            val code = CodeUtil.getCode(apiResult.code)

            return TestResult.ErrorResult(code, code.MSG, ErrorResult.ERROR_TYPE_NON, null, null)
        }
        else
        {
            val gson = Gson()
            val type = object : TypeToken<OauthResult>()
            {}.type
            val reader = gson.toJson(result.body())
            val oauthResult = gson.fromJson<OauthResult>(reader, type)

            if (oauthResult !is OauthResult) return TestResult.ErrorResult(LOCAL_ERROR_TYPE_MISS, LOCAL_ERROR_TYPE_MISS.MSG, ErrorResult.ERROR_TYPE_NON, null, null)

            SopoLog.d( msg = "requestRefreshOAuthToken => ${oauthResult}")

            CoroutineScope(Dispatchers.Default).launch {
                val entity = OauthMapper.objectToEntity(oauthResult)
                oauthRepoImpl.update(entity)
            }


            return TestResult.SuccessResult(SUCCESS, SUCCESS.MSG, oauthResult)
        }
    }

    private fun getRetrofitWithoutAuthenticator(response : Response, token : String) : Request
    {
        return response.request.newBuilder().removeHeader("Authorization")
            .addHeader("Authorization", "Bearer $token").build()
    }
}