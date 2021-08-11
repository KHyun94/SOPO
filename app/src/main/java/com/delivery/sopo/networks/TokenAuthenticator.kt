package com.delivery.sopo.networks

import com.delivery.sopo.BuildConfig
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.data.repository.local.o_auth.OAuthEntity
import com.delivery.sopo.enums.ResponseCode.*
import com.delivery.sopo.models.mapper.OAuthMapper
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.dto.OAuthDTO
import com.delivery.sopo.models.TestResult
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.api.OAuthAPI
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
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
    val userLocalRepository : UserLocalRepository by inject()
    val OAuthLocalRepository : OAuthLocalRepository by inject()

    override fun authenticate(route : Route?, response : Response) : Request?
    {
        if (response.code == 401)
        {
            SopoLog.d( msg = "authenticate call() - 401")

            when (val result = requestRefreshOAuthToken())
            {
                is TestResult.SuccessResult<*> ->
                {
                    val oauth = result.data as OAuthDTO
                    val accessToken = oauth.accessToken

                    SopoLog.d( msg = "authenticate success => $accessToken")

                    return getRetrofitWithoutAuthenticator(response, accessToken)
                }
                is TestResult.ErrorResult<*> ->
                {
                    SopoLog.e( msg = "authenticate fail => ${result.errorMsg}")
                }
            }
        }

        SopoLog.e( msg = "authenticate call() - else")
        return null
    }

    private fun requestRefreshOAuthToken() : TestResult
    {
        var OAuth : OAuthEntity?

        // ROOM 내 저장된 OAuth 데이터 호출
        runBlocking {
            withContext(Dispatchers.Default) {
                OAuth = OAuthLocalRepository.get(userLocalRepository.getUserId())
            }
        }

        // OAuth가 null 일 때 Error 호출
        if (OAuth == null) return TestResult.ErrorResult(ERROR_RESPONSE_DATA_IS_NULL, ERROR_RESPONSE_DATA_IS_NULL.MSG, ErrorResult.ERROR_TYPE_NON, null, null)

//        val retro = NetworkManager.retro("sopo-aos", "sopoAndroid!!@@").create(OAuthAPI::class.java)
        val retro = NetworkManager.retro(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_PASSWORD).create(OAuthAPI::class.java)
        val result = retro.requestRefreshOAuthToken(
            grantType = "refresh_token", email = userLocalRepository.getUserId(), refreshToken = OAuth?.refreshToken
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
            val type = object : TypeToken<OAuthDTO>() {}.type
            val reader = gson.toJson(result.body())
            val oauthResult = gson.fromJson<OAuthDTO>(reader, type)

            if (oauthResult !is OAuthDTO) return TestResult.ErrorResult(LOCAL_ERROR_TYPE_MISS, LOCAL_ERROR_TYPE_MISS.MSG, ErrorResult.ERROR_TYPE_NON, null, null)

            SopoLog.d( msg = "requestRefreshOAuthToken => ${oauthResult}")

            CoroutineScope(Dispatchers.Default).launch {
                val entity = OAuthMapper.objectToEntity(oauthResult)
                OAuthLocalRepository.update(entity)
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