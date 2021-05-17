package com.delivery.sopo.models

import com.google.gson.annotations.SerializedName

data class EmailAuthDTO(
    @SerializedName("code")
    val code: String,
    @SerializedName("token")
    val token: String
)