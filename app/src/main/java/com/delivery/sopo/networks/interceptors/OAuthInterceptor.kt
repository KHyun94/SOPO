package com.delivery.sopo.networks.interceptors

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class OAuthInterceptor(private val token : String): Interceptor
{
    override fun intercept(chain: Interceptor.Chain): Response
    {
        val request = chain.request()
        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer $token").build()
        return chain.proceed(authenticatedRequest)
    }
}