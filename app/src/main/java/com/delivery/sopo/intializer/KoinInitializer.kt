package com.delivery.sopo.intializer

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.delivery.sopo.di.*
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.BuildConfig
import com.orhanobut.logger.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

/**
 * Logger 기본 세팅. 초기 시작 시 동작되도록 InitializationProvider에 설정
 *
 */
class KoinInitializer : Initializer<KoinApplication>
{
    override fun create(context: Context): KoinApplication
    {
        return startKoin {
            androidContext(context)
            modules(listOf(apiModule, serviceModule, viewModelModule, useCaseModule, sourceModule, dbModule))
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = mutableListOf()
}