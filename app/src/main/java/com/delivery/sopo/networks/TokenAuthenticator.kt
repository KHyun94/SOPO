package com.delivery.sopo.networks

import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.koin.core.KoinComponent
import org.koin.core.inject

// TODO 401 에러 발생 이슈 있음
class TokenAuthenticator: Authenticator, KoinComponent
{
    val userRemoteRepo: UserRemoteRepository by inject()

    override fun authenticate(route: Route?, response: Response): Request?
    {
        SopoLog.i("TokenAuthenticator(...) 호출")

        if(response.code != 401)
        {
            SopoLog.e(msg = "authenticate call() - else")
            return null
        }

        SopoLog.d(msg = "authenticate call() - 401 ${response.message}")

        return try
        {
            val refreshOAuthToken = runBlocking(Dispatchers.IO) {
                userRemoteRepo.refreshOAuthToken()
            }

            getRetrofitWithoutAuthenticator(response, refreshOAuthToken.accessToken)
        }
        catch(e: Exception)
        {
            SopoLog.e("2차 오류 / ${e.toString()}", e)
            null
        }
    }

    private fun getRetrofitWithoutAuthenticator(response: Response, token: String): Request
    {
        return response.request.newBuilder()
            .removeHeader("Authorization")
            .addHeader("Authorization", "Bearer $token")
            .build()
    }
}