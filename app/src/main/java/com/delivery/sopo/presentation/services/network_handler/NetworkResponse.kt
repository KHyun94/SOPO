package com.delivery.sopo.presentation.services.network_handler

data class NetworkResponse<T>(
        val statusCode: Int,
        val data:T?
)