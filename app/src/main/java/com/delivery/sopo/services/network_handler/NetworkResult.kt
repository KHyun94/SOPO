package com.delivery.sopo.services.network_handler

sealed class NetworkResult<out T : Any> {
    data class Success<out T : Any>(val statusCode:Int, val data: T) : NetworkResult<T>()
    data class Error(val statusCode:Int?, val exception: Exception) : NetworkResult<Nothing>()
}