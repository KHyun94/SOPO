package com.delivery.sopo.models

import com.google.gson.annotations.SerializedName

data class OauthResult(
    @SerializedName("access_token")
    val accessToken : String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("expires_in")
    val expiresIn: String,
    @SerializedName("scope")
    val scope: String,
    @SerializedName("refresh_token_expire_at")
    val refreshTokenExpiredAt: String
)
