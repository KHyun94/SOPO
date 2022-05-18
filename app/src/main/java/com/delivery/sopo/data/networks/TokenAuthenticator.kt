package com.delivery.sopo.data.networks

import com.delivery.sopo.data.repositories.user.UserRepository
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
    var retryCnt: Int = 0

    val userRepository: UserRepository by inject()

    override fun authenticate(route: Route?, response: Response): Request?
    {
        SopoLog.i("TokenAuthenticator(...) 호출")

        if(response.code != 401)
        {
            SopoLog.e(msg = "authenticate call() - else")
            return null
        }

        SopoLog.d(msg = "authenticate call() - 401 ${response.message}")

        if(retryCnt > 2) return null

        return try
        {
            val refreshOAuthToken = runBlocking(Dispatchers.IO) {
                userRepository.refreshToken()
            }

            retryCnt += 1

            getRetrofitWithoutAuthenticator(response, refreshOAuthToken)
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