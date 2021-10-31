package com.delivery.sopo.exceptions

import com.delivery.sopo.models.api.ErrorResponse

class OAuthException:Exception
{
    private val statusCode: Int
    private val errorResponse: ErrorResponse

    constructor(statusCode: Int, errorResponse: ErrorResponse){
        this.statusCode = statusCode
        this.errorResponse = errorResponse
    }

    fun getStatusCode(): Int{
        return statusCode
    }

    fun getErrorResponse(): ErrorResponse
    {
        return errorResponse
    }
}