package com.delivery.sopo.models.api

import com.delivery.sopo.enums.ErrorType
import com.google.gson.annotations.SerializedName

data class ErrorResponse(
        @SerializedName("code")
        val code: Int,
        @SerializedName("type")
        val type: ErrorType,
        @SerializedName("message")
        val message: String,
        @SerializedName("path")
        var path: String
)

