package com.delivery.sopo.exceptions

import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.models.api.Error

class SOPOApiException(val statusCode: Int, val error: Error): Exception()
{
    override val message: String
        get() = error.message

    val code: ErrorCode
    get() = ErrorCode.getCode(error.code)

    companion object{
        fun create(code: Int, message: String?= null): SOPOApiException{
            val errorCode = ErrorCode.getCode(code)
            val error = with(errorCode) { Error(code, errorType, message ?: this.message, "") }
            return SOPOApiException(statusCode = errorCode.httpStatusCode, error = error)
        }

        fun create(errorCode: ErrorCode, message: String? = null): SOPOApiException{
            val error = with(errorCode) { Error(code, errorType, message ?: this.message, "") }
            return SOPOApiException(statusCode = errorCode.httpStatusCode, error = error)
        }
    }
}
