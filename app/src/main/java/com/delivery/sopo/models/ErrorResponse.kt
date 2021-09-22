package com.delivery.sopo.models

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
        @SerializedName("code") val code: Int,
        @SerializedName("type") val type: String,
        @SerializedName("message") val message: String,
        @SerializedName("path") var path: String
        )