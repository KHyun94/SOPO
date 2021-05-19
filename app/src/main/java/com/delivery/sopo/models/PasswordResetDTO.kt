package com.delivery.sopo.models

import com.google.gson.annotations.SerializedName

data class PasswordResetDTO(
    @SerializedName("resetToken")
    val resetToken: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)