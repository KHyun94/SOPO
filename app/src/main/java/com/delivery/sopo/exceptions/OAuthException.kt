package com.delivery.sopo.exceptions

import com.delivery.sopo.models.api.Error

class OAuthException(private val statusCode: Int, private val error: Error): Exception()
{
    fun getStatusCode(): Int = statusCode
    fun getErrorResponse(): Error = error
}