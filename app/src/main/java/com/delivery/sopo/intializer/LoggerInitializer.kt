package com.delivery.sopo.intializer

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.BuildConfig
import com.orhanobut.logger.Logger

/**
 * Logger 기본 세팅. 초기 시작 시 동작되도록 InitializationProvider에 설정
 *
 */
class LoggerInitializer : Initializer<Unit>
{
    override fun create(context: Context)
    {
        Logger.addLogAdapter(object: AndroidLogAdapter()
                             {
                                 override fun isLoggable(priority: Int, tag: String?): Boolean
                                 {
                                     return true
                                 }
                             })

        if(BuildConfig.DEBUG)
        { //로그 관련 세팅


        }


    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}