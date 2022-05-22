package com.delivery.sopo.exceptions

class UnknownException(private val statusCode: Int, private val e: Exception): Exception()
{
    fun getStatusCode(): Int = statusCode
    fun getException(): Exception = e
}