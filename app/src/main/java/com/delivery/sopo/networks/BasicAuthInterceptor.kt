package com.delivery.sopo.networks

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class BasicAuthInterceptor(private val userId :String, private val userPassword: String): Interceptor
{
    override fun intercept(chain: Interceptor.Chain): Response
    {
        val request = chain.request()
        val authenticatedRequest = request.newBuilder()
            .header("Authorization", Credentials.basic(userId, userPassword)).build()
        return chain.proceed(authenticatedRequest)
    }
}
