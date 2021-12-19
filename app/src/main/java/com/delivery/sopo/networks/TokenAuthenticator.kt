package com.delivery.sopo.networks

import com.delivery.sopo.enums.ResponseCode.*
import com.delivery.sopo.models.dto.OAuthDTO
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
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

        SopoLog.d(msg = "authenticate call() - 401")

        return runBlocking {
            try
            {
                with(userRemoteRepo.refreshOAuthToken()){ getRetrofitWithoutAuthenticator(response, accessToken) }
            }
            catch(e:Exception)
            {
                null
            }
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