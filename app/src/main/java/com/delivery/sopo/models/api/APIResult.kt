package com.delivery.sopo.models.api

import com.google.gson.annotations.SerializedName

data class APIResult<T>(
    @SerializedName("timestamp")
    var timestamp: String,
    @SerializedName("uniqueCode")
    var uniqueCode: String,
    @SerializedName("code")
    var code: String,
    @SerializedName("message")
    var message: String,
    @SerializedName("path")
    var path: String,
    @SerializedName("data")
    var data: T?
)