package com.delivery.sopo.models.user

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResetAuthCode(
        @SerializedName("resetToken")
        val resetToken: String,
        @SerializedName("authCode")
        val authCode: String,
        @SerializedName("email")
        val email: String,
): Serializable
