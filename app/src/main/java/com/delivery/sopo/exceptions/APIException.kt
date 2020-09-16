package com.delivery.sopo.exceptions

import com.delivery.sopo.enums.ResponseCode
import java.lang.RuntimeException

class APIException : RuntimeException
{
    val responseEnum: ResponseCode
    val httpStatus: Int
    override val message: String

    constructor(responseCode: ResponseCode)
    {
        this.responseEnum = responseCode
        this.message = responseCode.MSG
        this.httpStatus = responseCode.HTTP_STATUS
    }

    constructor(responseCode: ResponseCode, extraMessage: String)
    {
        this.responseEnum = responseCode
        this.message = responseCode.MSG + " : " + extraMessage
        this.httpStatus = responseCode.HTTP_STATUS
    }
}
