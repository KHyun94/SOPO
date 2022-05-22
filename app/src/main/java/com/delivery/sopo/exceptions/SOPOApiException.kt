package com.delivery.sopo.exceptions

import com.delivery.sopo.models.api.Error

class SOPOApiException(private val statusCode: Int, private val error: Error): Exception()
{
    fun getStatusCode(): Int = statusCode
    fun getError(): Error = error
}
