package com.delivery.sopo.models.user

import com.google.gson.annotations.SerializedName

data class ResetPassword(
    @SerializedName("resetToken")
    val resetToken: String,
    @SerializedName("authCode")
    val authCode: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)