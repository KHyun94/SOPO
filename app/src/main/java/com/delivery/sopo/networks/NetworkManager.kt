package com.delivery.sopo.networks

import com.delivery.sopo.BuildConfig
import com.delivery.sopo.networks.interceptors.BasicAuthInterceptor
import com.delivery.sopo.networks.interceptors.OAuthInterceptor
import com.delivery.sopo.util.SopoLog
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkManager
{
    val TAG = this.javaClass.simpleName
    private const val CONNECT_TIMEOUT: Long = 15
    private const val WRITE_TIMEOUT: Long = 15
    private const val READ_TIMEOUT: Long = 15

    lateinit var mOKHttpClient: OkHttpClient

    lateinit var apiId: String
    lateinit var apiPassword: String

    var hasHeader: Boolean = false

    var INTERCEPTOR_TYPE = 0
    var isAuthenticator = true

    fun setLogin(id: String?, password: String?) = if (id != null && password != null)
    {
        this.apiId = id
        this.apiPassword = password
        hasHeader = true
    }
    else
    {
        hasHeader = false
    }

    fun retro(vararg params : String? = emptyArray()) : Retrofit
    {
        INTERCEPTOR_TYPE = params.size
        val interceptor : Interceptor? = when(INTERCEPTOR_TYPE)
        {
            1 -> OAuthInterceptor(params[0]!!)   // 파라미터 갯수 1일 때 OAuthInterceptor(Token)
            2 -> BasicAuthInterceptor(params[0]!!, params[1]!!)   // 파라미터 갯수 2일 때 BasicAuthInterceptor (userId or userPassword)
            else -> null
        }

        SopoLog.d(tag = TAG, msg = "네트워크 인증 타입 => $INTERCEPTOR_TYPE")

        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        mOKHttpClient = OkHttpClient().newBuilder().apply {
            addInterceptor(httpLoggingInterceptor)
            if(interceptor != null) addInterceptor(interceptor)
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            if(INTERCEPTOR_TYPE == 1 && isAuthenticator) authenticator(TokenAuthenticator())
        }.build()

        val gson = GsonBuilder().apply {
            setLenient()
        }

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(mOKHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson.create()))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }


    val retro: Retrofit
        get()
        {
            // 공용 API 계정


            val basicAuthInterceptor : Interceptor? = if(hasHeader) BasicAuthInterceptor(apiId, apiPassword) else null

            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            mOKHttpClient = OkHttpClient().newBuilder().apply {
                addInterceptor(httpLoggingInterceptor)
                if(basicAuthInterceptor != null) addInterceptor(basicAuthInterceptor)
                connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            }.build()

            val gson = GsonBuilder().apply {
                setLenient()
            }

            return Retrofit.Builder()
                .baseUrl(BuildConfig.API_URL)
                .client(mOKHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson.create()))
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        }

}