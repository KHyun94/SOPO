package com.delivery.sopo.enums

sealed class NetworkStatus
{
    object Default: NetworkStatus()
    object Wifi: NetworkStatus()
    object Cellular: NetworkStatus()
    object NotConnect: NetworkStatus()
}