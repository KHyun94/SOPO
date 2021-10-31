package com.delivery.sopo.services.network_handler

import com.delivery.sopo.models.api.ErrorResponse

data class NetworkResponse<T>(
        val statusCode: Int,
        val data:T?
)