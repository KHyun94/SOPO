package com.delivery.sopo.networks

import com.delivery.sopo.consts.NetworkConst
import com.delivery.sopo.repository.UserRepo
import com.google.gson.GsonBuilder
import com.kakao.usermgmt.response.model.User
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.java.KoinJavaComponent.inject

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

object NetworkManager {

    private val CONNECT_TIMEOUT: Long = 15
    private val WRITE_TIMEOUT: Long = 15
    private val READ_TIMEOUT: Long = 15

    lateinit var mOKHttpClient: OkHttpClient
    lateinit var mRetrofit: Retrofit

    fun getPrivateUserAPI(id:String, pwd:String):UserAPI {
        val basicAuthInterceptor = BasicAuthInterceptor(id, pwd)
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        mOKHttpClient = OkHttpClient().newBuilder().apply {
            addInterceptor(httpLoggingInterceptor)
            addInterceptor(basicAuthInterceptor)
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        }.build()

        val gson = GsonBuilder()
        gson.setLenient()

        mRetrofit = Retrofit.Builder()
            .baseUrl(NetworkConst.API_URL)
            .client(mOKHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson.create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        return mRetrofit.create(UserAPI::class.java)
    }


    fun getUserAPI(): UserAPI {
        // 공용 API 계정
        val basicAuthInterceptor = BasicAuthInterceptor(NetworkConst.REAL_API_ID, NetworkConst.REAL_API_PWD)
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        mOKHttpClient = OkHttpClient().newBuilder().apply {
            addInterceptor(httpLoggingInterceptor)
            addInterceptor(basicAuthInterceptor)
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        }.build()

        val gson = GsonBuilder()
        gson.setLenient()

        mRetrofit = Retrofit.Builder()
            .baseUrl(NetworkConst.API_URL)
            .client(mOKHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson.create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        return mRetrofit.create(UserAPI::class.java)
    }
}