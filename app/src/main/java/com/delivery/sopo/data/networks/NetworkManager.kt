package com.delivery.sopo.data.networks

import com.delivery.sopo.BuildConfig
import com.delivery.sopo.data.models.AuthToken
import com.delivery.sopo.data.networks.interceptors.BasicAuthInterceptor
import com.delivery.sopo.data.networks.interceptors.AuthInterceptor
import com.delivery.sopo.data.repositories.user.UserRepository
import com.delivery.sopo.data.resources.auth.local.AuthDataSource
import com.delivery.sopo.enums.NetworkEnum
import com.delivery.sopo.util.SopoLog
import com.google.gson.GsonBuilder
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.orhanobut.logger.Logger.VERBOSE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkManager: KoinComponent
{
    private val authDataSource: AuthDataSource by inject()
    private val userRepository: UserRepository by inject()
    private const val CONNECT_TIMEOUT: Long = 15
    private const val WRITE_TIMEOUT: Long = 15
    private const val READ_TIMEOUT: Long = 15

    lateinit var mOKHttpClient: OkHttpClient

    var INTERCEPTOR_TYPE = 0
    var isAuthenticator = true

    fun <T> setLoginMethod(method: NetworkEnum, clz: Class<T>): T
    {
        return when(method)
        {
            NetworkEnum.O_AUTH_TOKEN_LOGIN ->
            {
                val authToken: AuthToken.Info =
                    runBlocking(Dispatchers.Default) { authDataSource.get() }
                retro(authToken.accessToken).create(clz)
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

    fun retro(vararg params: String? = emptyArray()): Retrofit
    {
        INTERCEPTOR_TYPE = params.size

        SopoLog.d(msg = "네트워크 인증 타입 => $INTERCEPTOR_TYPE ${params.joinToString()}")
        val interceptor: Interceptor? = when(INTERCEPTOR_TYPE)
        {
            1 -> AuthInterceptor(authDataSource)   // 파라미터 갯수 1일 때 OAuthInterceptor(Token)
            2 -> BasicAuthInterceptor(params[0]!!, params[1]!!)   // 파라미터 갯수 2일 때 BasicAuthInterceptor (userId or userPassword)
            else -> null
        }

        val httpLoggingInterceptor = LoggingInterceptor.Builder()
            .setLevel(Level.BASIC)
            .tag("SOPO_NETWORK")
            .log(VERBOSE)
            .build()

        mOKHttpClient = OkHttpClient().newBuilder().apply {
            addInterceptor(httpLoggingInterceptor)
            if(interceptor != null) addInterceptor(interceptor)
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            followRedirects(false)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            if(INTERCEPTOR_TYPE == 1 && isAuthenticator) authenticator(TokenAuthenticator(authDataSource, userRepository))
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