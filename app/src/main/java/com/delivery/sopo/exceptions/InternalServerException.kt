package com.delivery.sopo.exceptions

import com.delivery.sopo.enums.ErrorType
import com.delivery.sopo.models.api.Error

class InternalServerException : Exception
{
    override var message: String = ""
    var statusCode: Int = 500
    private lateinit var e: Exception
    private lateinit var error: Error

    constructor(message: String){
        this.message = message
    }

    constructor(message: String, e: Exception){
        this.message = message
        this.e = e
    }

    constructor(message: String, error: Error){
        this.message = message
        this.error = error
    }

    fun getException(): Exception
    {
        if(!::e.isInitialized) return Exception("초기화되지 않은 에러입니다.")
        return e
    }

    fun getErrorResponse(): Error
    {
        if(!::error.isInitialized) return Error(999, ErrorType.LOCAL, "초기화되지 않은 데이터입니다.", "")
        return error
    }
}