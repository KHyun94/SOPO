package com.delivery.sopo.services.network_handler

sealed class Result<out T : Any> {
    data class Success<out T : Any>(val statusCode:Int, val data: T) : Result<T>()
    data class Error(val statusCode:Int?, val exception: Exception) : Result<Nothing>()
}