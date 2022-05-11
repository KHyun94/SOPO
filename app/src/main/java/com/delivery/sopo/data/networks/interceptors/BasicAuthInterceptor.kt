package com.delivery.sopo.data.networks.interceptors

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class BasicAuthInterceptor(private val clientId :String, private val clientPassword: String): Interceptor
{
    override fun intercept(chain: Interceptor.Chain): Response
    {
        val request = chain.request()
        val authenticatedRequest = request.newBuilder()
            .header("Authorization", Credentials.basic(clientId, clientPassword)).build()
        return chain.proceed(authenticatedRequest)
    }
}
