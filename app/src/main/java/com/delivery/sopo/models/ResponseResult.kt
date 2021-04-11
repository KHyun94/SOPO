package com.delivery.sopo.models

import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ResponseCode

class ResponseResult<T>(val result:Boolean, val code: ResponseCode?= null, val data: T, val message: String, val displayType: DisplayEnum = DisplayEnum.NON_DISPLAY)