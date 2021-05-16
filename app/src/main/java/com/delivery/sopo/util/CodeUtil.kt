package com.delivery.sopo.util

import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.enums.ResponseCode.*

object CodeUtil
{
    fun getCode(code : String?)  : ResponseCode
    {
        if(code == null) return UNKNOWN_ERROR

        val list = enumValues<ResponseCode>()
        var value = UNKNOWN_ERROR

        list.forEach {
            if(it.CODE == code) value = it
        }

        return value
    }
}