package com.delivery.sopo.models

import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ResponseCode

sealed class Response<T>
{
    data class Success<T>(val code: ResponseCode, val data:T): Response<T>()
    data class Failure<E>(val code: ResponseCode, val message: String, val data:E): Response<E>()
}
