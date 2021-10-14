package com.delivery.sopo.services.network_handler

import com.delivery.sopo.models.api.ErrorResponse

sealed class NetworkResponseBeta<out T : Any> {
    data class Success<out T : Any>(val statusCode:Int, val data: T) : NetworkResponseBeta<T>()
    data class SuccessNoBody(val statusCode: Int): NetworkResponseBeta<Nothing>()
    data class Error(val statusCode:Int = 500, val errorResponse: ErrorResponse) : NetworkResponseBeta<Nothing>()
}