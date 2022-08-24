package com.delivery.sopo.data.networks

import com.delivery.sopo.data.repositories.user.UserRepository
import com.delivery.sopo.data.resources.auth.local.AuthDataSource
import com.delivery.sopo.data.resources.auth.remote.AuthRemoteDataSource
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.koin.core.KoinComponent

// TODO 401 에러 발생 이슈 있음
class TokenAuthenticator(private val authDataSource: AuthDataSource, private val authRemoteDataSource: AuthRemoteDataSource): Authenticator, KoinComponent
{
    var retryCnt: Int = 0

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
            val accessToken = runBlocking(Dispatchers.IO) {
                val refreshToken = authDataSource.get().refreshToken
                val tokenInfo = authRemoteDataSource.refreshToken(refreshToken = refreshToken)

                withContext(Dispatchers.Default) { authDataSource.insert(token = tokenInfo) }

                return@runBlocking tokenInfo.accessToken
            }

            getRetrofitWithoutAuthenticator(response, accessToken)
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