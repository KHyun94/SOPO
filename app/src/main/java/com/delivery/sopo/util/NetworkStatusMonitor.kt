package com.delivery.sopo.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import com.delivery.sopo.SOPOApplication
import com.delivery.sopo.enums.NetworkStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class NetworkStatusMonitor(private val context: Context, private val networkStatus: MutableStateFlow<NetworkStatus>): ConnectivityManager.NetworkCallback()
{

    init
    {
        SopoLog.i("NetworkStatusMonitor init(...)")
    }

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /** Network를 감지할 Capabilities 선언 **/
    private val networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    /** 네트워크 상태 체크 **/
    fun initNetworkCheck()
    {
        val activeNetwork = connectivityManager.activeNetwork
        val connectivityStatus = getConnectivityStatus(context = context)
        SopoLog.d("Init Network Status[${connectivityStatus}]")
        networkStatus.value = connectivityStatus
    }

    /** Network 모니터링 서비스 시작 **/
    fun enable()
    {
        connectivityManager.registerNetworkCallback(networkRequest, this)
    }

    //** Network 모니터링 서비스 해제 **//*
    fun disable()
    {
        connectivityManager.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network)
    {
        super.onAvailable(network)

        val connectivityStatus = getConnectivityStatus(context = context)
        SopoLog.d("Network onAvailable Status[${connectivityStatus}]")
        networkStatus.value = connectivityStatus
    }

    override fun onLost(network: Network)
    {
        super.onLost(network)

        val connectivityStatus = getConnectivityStatus(context = context)
        SopoLog.d("Network onLost Status[${connectivityStatus}]")
        networkStatus.value = connectivityStatus
    }


    fun getConnectivityStatus(context: Context): NetworkStatus
    {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // 활성화된 네트워크의 상태를 표현하는 객체
            val nc = cm.getNetworkCapabilities(cm.activeNetwork) ?: return NetworkStatus.NotConnect

            return when
            {
                nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                {
                    NetworkStatus.Wifi
                }
                nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                {
                    NetworkStatus.Cellular
                }
                else -> NetworkStatus.NotConnect
            }
        }

        val activeNetwork = cm.activeNetworkInfo ?: return NetworkStatus.NotConnect

        return when(activeNetwork.type)
        {
            ConnectivityManager.TYPE_WIFI ->
            {
                NetworkStatus.Wifi
            }
            ConnectivityManager.TYPE_MOBILE ->
            {
                NetworkStatus.Cellular
            }
            else -> NetworkStatus.NotConnect
        }
    }
}