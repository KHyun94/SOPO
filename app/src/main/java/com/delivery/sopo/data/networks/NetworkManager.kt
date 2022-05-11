package com.delivery.sopo.data.networks

import com.delivery.sopo.BuildConfig
import com.delivery.sopo.enums.NetworkEnum
import com.delivery.sopo.data.networks.interceptors.BasicAuthInterceptor
import com.delivery.sopo.data.networks.interceptors.OAuthInterceptor
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.models.dto.OAuthToken
import com.delivery.sopo.util.SopoLog
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkManager : KoinComponent
{
    private val userLocalRepo : UserLocalRepository by inject()
    private val oAuthLocalRepo : OAuthLocalRepository by inject()

    private const val CONNECT_TIMEOUT: Long = 15
    private const val WRITE_TIMEOUT: Long = 15
    private const val READ_TIMEOUT: Long = 15

    lateinit var mOKHttpClient: OkHttpClient

    var INTERCEPTOR_TYPE = 0
    var isAuthenticator = true

    fun<T> setLoginMethod(method : NetworkEnum, clz : Class<T>) : T
    {
        return when(method)
        {
            NetworkEnum.O_AUTH_TOKEN_LOGIN ->
            {
                val oAuth : OAuthToken = runBlocking(Dispatchers.Default) { oAuthLocalRepo.get(userId = userLocalRepo.getUserId()) }
                retro(oAuth.accessToken).create(clz)
            }
            NetworkEnum.PUBLIC_LOGIN ->
            {
                retro(BuildConfig.PUBLIC_API_ACCOUNT_ID, BuildConfig.PUBLIC_API_ACCOUNT_PASSWORD).create(clz)
            }
            NetworkEnum.PRIVATE_LOGIN ->
            {
                retro(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_PASSWORD).create(clz)
            }
            NetworkEnum.EMPTY_LOGIN ->
            {
                retro().create(clz)
            }
        }
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

        SopoLog.d( msg = "네트워크 인증 타입 => $INTERCEPTOR_TYPE")

        val httpLoggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger{
            override fun log(message: String)
            {
                SopoLog.api(message)
            }
        })

        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        mOKHttpClient = OkHttpClient().newBuilder().apply {
            addInterceptor(httpLoggingInterceptor)
            if(interceptor != null) addInterceptor(interceptor)
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            followRedirects(false)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            if(INTERCEPTOR_TYPE == 1 && isAuthenticator) authenticator(TokenAuthenticator())
        }.build()

        val gson = GsonBuilder().apply {
            setLenient()
        }

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
//            .baseUrl("http://172.20.10.2:6443/")
            .client(mOKHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson.create()))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }
}