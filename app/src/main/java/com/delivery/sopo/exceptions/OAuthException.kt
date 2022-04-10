package com.delivery.sopo.exceptions

import com.delivery.sopo.models.api.ErrorResponse

class OAuthException(private val statusCode: Int, private val errorResponse: ErrorResponse): Exception()
{
    fun getStatusCode(): Int = statusCode
    fun getErrorResponse(): ErrorResponse = errorResponse
}