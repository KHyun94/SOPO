package com.delivery.sopo.models

import com.google.gson.annotations.SerializedName

data class APIAccount(
    @SerializedName("API_ID")
    val api_id: String,
    @SerializedName("API_PWD")
    val api_pwd: String
) {
}