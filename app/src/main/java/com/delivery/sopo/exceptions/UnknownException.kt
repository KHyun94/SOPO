package com.delivery.sopo.exceptions

import com.delivery.sopo.models.api.ErrorResponse

class UnknownException(private val statusCode: Int, private val e: Exception): Exception()
{
    fun getStatusCode(): Int = statusCode
    fun getException(): Exception = e
}