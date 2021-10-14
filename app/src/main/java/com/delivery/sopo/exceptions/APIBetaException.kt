package com.delivery.sopo.exceptions

import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.api.ErrorResponse
import com.delivery.sopo.util.CodeUtil
import com.google.gson.Gson
import retrofit2.Response

class APIBetaException: Exception
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
