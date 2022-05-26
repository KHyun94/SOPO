package com.delivery.sopo.data.networks

import android.app.Application
import com.delivery.sopo.BuildConfig
import com.delivery.sopo.data.networks.interceptors.AuthInterceptor
import com.delivery.sopo.data.networks.serivces.ParcelService
import com.delivery.sopo.data.resources.auth.local.AuthDataSource
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object APIClient
{
    private const val CONNECT_TIMEOUT: Long = 15
    private const val WRITE_TIMEOUT: Long = 15
    private const val READ_TIMEOUT: Long = 15

    fun provideParcelService(retrofit: Retrofit): ParcelService
    {
        return retrofit.create(ParcelService::class.java)
    }

    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit
    {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(getGson()))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }
    fun providePublicOkHttpClient(cache: Cache): OkHttpClient
    {
        return OkHttpClient().newBuilder().apply {
            addInterceptor(getHttpLoggingInterceptor())
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            cache(cache = cache)
            followRedirects(false)
        }.build()
    }

    fun providePrivateOkHttpClient(cache: Cache, authDataSource: AuthDataSource): OkHttpClient
    {
        return OkHttpClient().newBuilder().apply {
            connectTimeout(timeout = CONNECT_TIMEOUT, unit = TimeUnit.SECONDS)
            writeTimeout(timeout = WRITE_TIMEOUT, unit = TimeUnit.SECONDS)
            readTimeout(timeout = READ_TIMEOUT, unit = TimeUnit.SECONDS)
            cache(cache = cache)
            followRedirects(followRedirects = false)
            addInterceptor(interceptor = getHttpLoggingInterceptor())
            addInterceptor(interceptor = getAuthInterceptor(authDataSource = authDataSource))
            authenticator(authenticator = getTokenAuthenticator())
        }.build()
    }

    fun provideCache(application: Application): Cache { return Cache(application.cacheDir, 10L * 1024 * 1024) }

    fun getGson(): Gson = GsonBuilder().apply { setLenient() }.create()

    fun getHttpLoggingInterceptor(): HttpLoggingInterceptor
    {
        val interceptor = HttpLoggingInterceptor { message -> SopoLog.api(message) }
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }
    fun getAuthInterceptor(authDataSource: AuthDataSource) = AuthInterceptor(authDataSource)
    fun getTokenAuthenticator() = TokenAuthenticator()
}