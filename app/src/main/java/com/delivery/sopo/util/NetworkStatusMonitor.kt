package com.delivery.sopo.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.enums.NetworkStatus


class NetworkStatusMonitor(private val activity: AppCompatActivity): ConnectivityManager.NetworkCallback()
{
    private val connectivityManager =
        activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /** Network를 감지할 Capabilities 선언 **/
    private val networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    /** 네트워크 상태 체크 **/
    fun initNetworkCheck()
    {
        val activeNetwork = connectivityManager.activeNetwork

        if(activeNetwork != null)
        {
            SopoLog.d("네트워크 연결되어있음 + ${getConnectivityStatus(context = activity)}")
            SOPOApp.networkStatus.postValue(getConnectivityStatus(context = activity))
        }
        else
        {
            SopoLog.d("네트워크 연결되어 있지않음 + ${getConnectivityStatus(context = activity)}")
            SOPOApp.networkStatus.postValue(getConnectivityStatus(context = activity))
        }
    }

    /** Network 모니터링 서비스 시작 **/
    fun enable()
    {
        //        check(activity.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED))
        connectivityManager.registerNetworkCallback(networkRequest, this)
    }

    //** Network 모니터링 서비스 해제 **//*
    fun disable()
    {
        connectivityManager.unregisterNetworkCallback(this)
        //        activity.lifecycle.removeObserver(activity)
    }

    /** Network가 Available 상태이면 Call **/
    override fun onAvailable(network: Network)
    {
        super.onAvailable(network)
        SopoLog.d("networkAvailable() + ${getConnectivityStatus(context = activity)}")
        SOPOApp.networkStatus.postValue(getConnectivityStatus(context = activity))
    }

    /** Network가 Available 상태에서 Unavailable로 변경되면 Call **/
    override fun onLost(network: Network)
    {
        super.onLost(network)
        SopoLog.d("networkUnavailable() + ${getConnectivityStatus(context = activity)}")
        SOPOApp.networkStatus.postValue(getConnectivityStatus(context = activity))
    }


    fun getConnectivityStatus(context: Context): NetworkStatus
    {
        // 네트워크 연결 상태 확인하기 위한 ConnectivityManager 객체 생성
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // 활성화된 네트워크의 상태를 표현하는 객체
            val nc = cm.getNetworkCapabilities(cm.activeNetwork) ?: return NetworkStatus.NOT_CONNECT

            return when
            {
                nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                {
                    NetworkStatus.WIFI
                }
                nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                {
                    NetworkStatus.CELLULAR
                }
                else -> NetworkStatus.NOT_CONNECT
            }
        }

        // 기기 버전이 마시멜로우 버전보다 아래인 경우
        // getActiveNetworkInfo -> API level 29에 디플리케이트 됨
        val activeNetwork = cm.activeNetworkInfo ?: return NetworkStatus.NOT_CONNECT

        return when(activeNetwork.type)
        {
            ConnectivityManager.TYPE_WIFI ->
            {
                NetworkStatus.WIFI
            }
            ConnectivityManager.TYPE_MOBILE ->
            {
                NetworkStatus.CELLULAR
            }
            else -> NetworkStatus.NOT_CONNECT
        }
    }
}