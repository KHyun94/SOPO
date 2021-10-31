package com.delivery.sopo.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest


class NetworkConnectionCheck(val context: Context): ConnectivityManager.NetworkCallback()
{
    val networkRequest: NetworkRequest by lazy {
        NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
    }
    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun register()
    {
        connectivityManager.registerNetworkCallback(networkRequest, this)
    }

    fun unregister(){
        connectivityManager.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network)
    {
        super.onAvailable(network)
    }

    override fun onLost(network: Network)
    {
        super.onLost(network)
    }

    fun isNetworkConnected(context: Context): Boolean
    {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        val wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val wimax = manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX)
        var bwimax = false
        if(wimax != null)
        {
            bwimax = wimax.isConnected
        }
        if(mobile != null)
        {
            if(mobile.isConnected || wifi!!.isConnected || bwimax)
            {
                return true
            }
        }
        else
        {
            if(wifi!!.isConnected || bwimax)
            {
                return true
            }
        }
        return false
    }
}