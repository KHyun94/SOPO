package com.delivery.sopo.models

import com.delivery.sopo.enums.ResponseCode

class ResponseResult<T>(val result:Boolean, val code: ResponseCode, val data: T, val message: String)