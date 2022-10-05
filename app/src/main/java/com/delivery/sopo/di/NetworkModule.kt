package com.delivery.sopo.di

import android.app.Application
import com.delivery.sopo.BuildConfig
import com.delivery.sopo.data.networks.TokenAuthenticator
import com.delivery.sopo.data.networks.interceptors.AuthInterceptor
import com.delivery.sopo.data.networks.interceptors.BasicAuthInterceptor
import com.delivery.sopo.data.networks.serivces.ParcelService
import com.delivery.sopo.data.networks.serivces.SignUpService
import com.delivery.sopo.data.networks.serivces.UserService
import com.delivery.sopo.data.resources.auth.local.AuthDataSource
import com.delivery.sopo.data.resources.auth.remote.AuthRemoteDataSource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.orhanobut.logger.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PublicAccess

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PrivateAccess

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule
{
    private const val CONNECT_TIMEOUT: Long = 15
    private const val WRITE_TIMEOUT: Long = 15
    private const val READ_TIMEOUT: Long = 15

    @Provides
    @Singleton
    fun provideSignUpService(@PublicAccess retrofit: Retrofit): SignUpService = retrofit.create(SignUpService::class.java)

    @Provides
    @Singleton
    @PublicAccess
    fun provideUserPublicService(@PublicAccess retrofit: Retrofit): UserService = retrofit.create(UserService::class.java)

    @Provides
    @Singleton
    @PrivateAccess
    fun provideUserPrivateService(@PrivateAccess retrofit: Retrofit): UserService = retrofit.create(UserService::class.java)

    @Provides
    @Singleton
    @PrivateAccess
    fun provideParcelService(@PrivateAccess retrofit: Retrofit): ParcelService = retrofit.create(ParcelService::class.java)


    @Provides
    @Singleton
    @PrivateAccess
    fun providePrivateRetrofit(@PrivateAccess okHttpClient: OkHttpClient): Retrofit
    {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(getGson()))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    @PublicAccess
    fun providePublicRetrofit(@PublicAccess okHttpClient: OkHttpClient): Retrofit
    {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(getGson()))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    @PublicAccess
    fun providePublicOkHttpClient(cache: Cache, basicAuthInterceptor: BasicAuthInterceptor,loggingInterceptor: LoggingInterceptor): OkHttpClient
    {
        return OkHttpClient().newBuilder().apply {
            addInterceptor(loggingInterceptor)
            addInterceptor(basicAuthInterceptor)
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            cache(cache = cache)
            followRedirects(false)
        }.build()
    }

    @Provides
    @Singleton
    @PrivateAccess
    fun providePrivateOkHttpClient(cache: Cache, authInterceptor: AuthInterceptor, loggingInterceptor: LoggingInterceptor, tokenAuthenticator: TokenAuthenticator): OkHttpClient
    {
        return OkHttpClient().newBuilder().apply {
            connectTimeout(timeout = CONNECT_TIMEOUT, unit = TimeUnit.SECONDS)
            writeTimeout(timeout = WRITE_TIMEOUT, unit = TimeUnit.SECONDS)
            readTimeout(timeout = READ_TIMEOUT, unit = TimeUnit.SECONDS)
            cache(cache = cache)
            followRedirects(followRedirects = false)
            addInterceptor(interceptor = loggingInterceptor)
            addInterceptor(interceptor = authInterceptor)
            authenticator(authenticator = tokenAuthenticator)
        }.build()
    }

    fun getGson(): Gson = GsonBuilder().apply { setLenient() }.create()

    @Provides
    @Singleton
    fun provideCache(application: Application) = Cache(application.cacheDir, 10L * 1024 * 1024)

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): LoggingInterceptor
    {
        return LoggingInterceptor.Builder()
            .setLevel(Level.BASIC)
            .tag("SOPO_NETWORK")
            .log(Logger.VERBOSE)
            .build()
    }

    @Provides
    @Singleton
    fun provideBasicAuthInterceptor() = BasicAuthInterceptor(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_PASSWORD)

    @Provides
    @Singleton
    fun provideTokenAuthInterceptor(authDataSource: AuthDataSource) = AuthInterceptor(authDataSource)

    @Provides
    @Singleton
    fun provideTokenAuthenticator(authDataSource: AuthDataSource, authRemoteDataSource: AuthRemoteDataSource) =
        TokenAuthenticator(authDataSource = authDataSource, authRemoteDataSource = authRemoteDataSource)
}