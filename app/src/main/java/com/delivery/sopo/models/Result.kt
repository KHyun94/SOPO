package com.delivery.sopo.models

import com.delivery.sopo.enums.ResponseCode

sealed class TestResult<out T : Any>
{
    data class SuccessResult<out T : Any>(val code : ResponseCode? = null, val successMsg : String, val data : T) : TestResult<T>()
    data class ErrorResult<out T : Any>(val code : ResponseCode? = null, val errorMsg : String, var errorType : Int, val data : T? = null, val e : Throwable? = null) : TestResult<T>()
}

data class Result<T, E>(var successResult : SuccessResult<T>? = null, var errorResult : ErrorResult<E>? = null)
