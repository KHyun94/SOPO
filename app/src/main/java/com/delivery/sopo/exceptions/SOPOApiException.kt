package com.delivery.sopo.exceptions

import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.models.api.Error

class SOPOApiException(val statusCode: Int, val error: Error): Exception()
{
    companion object{
        fun create(code: Int): SOPOApiException{
            val errorCode = ErrorCode.getCode(code)
            val error = with(errorCode) { Error(code, errorType, message, "") }
            return SOPOApiException(statusCode = errorCode.httpStatusCode, error = error)
        }

        fun create(errorCode: ErrorCode): SOPOApiException{
            val error = with(errorCode) { Error(code, errorType, message, "") }
            return SOPOApiException(statusCode = errorCode.httpStatusCode, error = error)
        }
    }
}
