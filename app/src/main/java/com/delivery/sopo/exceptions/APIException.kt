package com.delivery.sopo.exceptions

import com.delivery.sopo.enums.ResponseCodeEnum
import java.lang.RuntimeException

class APIException : RuntimeException
{
    val responseEnumEnum: ResponseCodeEnum
    val httpStatus: Int
    override val message: String

    constructor(responseCodeEnum: ResponseCodeEnum)
    {
        this.responseEnumEnum = responseCodeEnum
        this.message = responseCodeEnum.MSG
        this.httpStatus = responseCodeEnum.HTTP_STATUS
    }

    constructor(responseCodeEnum: ResponseCodeEnum, extraMessage: String)
    {
        this.responseEnumEnum = responseCodeEnum
        this.message = responseCodeEnum.MSG + " : " + extraMessage
        this.httpStatus = responseCodeEnum.HTTP_STATUS
    }
}
