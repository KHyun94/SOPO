package com.delivery.sopo.models

import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ResponseCode

data class ResponseResult<T>(val result:Boolean, val code: ResponseCode?= null, val data: T, val message: String, val displayType: DisplayEnum = DisplayEnum.NON_DISPLAY){

    companion object{
        fun<T> success(code: ResponseCode, data: T, message: String): ResponseResult<T>
        {
            return ResponseResult<T>(true, code, data, message)
        }

        fun<T> fail(code: ResponseCode, data: T? = null, message: String, displayType: DisplayEnum = DisplayEnum.NON_DISPLAY): ResponseResult<T?>
        {
            return ResponseResult<T?>(false, code, data, message, displayType)
        }
    }
}