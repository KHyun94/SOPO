package com.delivery.sopo.models

import com.delivery.sopo.enums.ResponseCode

data class SuccessResult<T>(
    val code: ResponseCode? = null,
    val successMsg: String,
    val data: T
)
