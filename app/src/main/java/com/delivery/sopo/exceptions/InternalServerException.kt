package com.delivery.sopo.exceptions

import com.delivery.sopo.enums.ErrorType
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.models.api.ErrorResponse
import com.delivery.sopo.util.SopoLog
import java.lang.ClassCastException

class InternalServerException : Exception
{
    override var message: String = ""
    var statusCode: Int = 500
    private lateinit var e: Exception
    private lateinit var errorResponse: ErrorResponse

    constructor(message: String){
        this.message = message
    }

    constructor(message: String, e: Exception){
        this.message = message
        this.e = e
    }

    constructor(message: String, errorResponse: ErrorResponse){
        this.message = message
        this.errorResponse = errorResponse
    }

    fun getException(): Exception
    {
        if(!::e.isInitialized) return Exception("초기화되지 않은 에러입니다.")
        return e
    }

    fun getErrorResponse(): ErrorResponse
    {
        if(!::errorResponse.isInitialized) return ErrorResponse(999, ErrorType.LOCAL, "초기화되지 않은 데이터입니다.", "")
        return errorResponse
    }
}