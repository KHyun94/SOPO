package com.delivery.sopo.util.fun_util

import android.content.Context
import android.net.ConnectivityManager
import com.delivery.sopo.consts.NetworkConst


object NetworkUtil {


    fun getConnectivityStatus(context: Context): Int
    {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = manager.activeNetworkInfo
        if (networkInfo != null) {
            val type = networkInfo.type

            when (type) {
                ConnectivityManager.TYPE_MOBILE ->
                    return NetworkConst.TYPE_MOBILE
                ConnectivityManager.TYPE_WIFI -> //와이파이 연결된것
                    return NetworkConst.TYPE_WIFI
            }
        }
        return NetworkConst.TYPE_NOT_CONNECTED  //연결이 되지않은 상태
    }
}