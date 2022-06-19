package com.delivery.sopo.data.networks.interceptors

import com.delivery.sopo.data.resources.auth.local.AuthDataSource
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val authDataSource: AuthDataSource): Interceptor
{
    fun getAccessToken() = runBlocking {
        return@runBlocking authDataSource.get().accessToken
    }

    override fun intercept(chain: Interceptor.Chain): Response
    {
        val request = chain.request()
        val authenticatedRequest = request.newBuilder().header("Authorization", "Bearer ${getAccessToken()}").build()
        return chain.proceed(authenticatedRequest)
    }
}