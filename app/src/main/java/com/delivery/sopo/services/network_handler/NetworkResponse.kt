package com.delivery.sopo.services.network_handler

data class NetworkResponse<T>(
        val statusCode: Int,
        val data:T?
)