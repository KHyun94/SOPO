package com.delivery.sopo.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class AuthToken
{
    data class Request(
            @SerializedName("username") val username: String,
            @SerializedName("password") val password: String,
            @SerializedName("deviceId") val deviceId: String): Serializable

    data class Refresh(
            @SerializedName("refreshToken") val refreshToken: String,
            @SerializedName("deviceId") val deviceId: String): Serializable

    data class Info(
            @SerializedName("grant_type") val grantType: String,
            @SerializedName("user_token") val userToken: String,
            @SerializedName("access_token") val accessToken: String,
            @SerializedName("refresh_token") val refreshToken: String,
            @SerializedName("expire_at") val expireAt: String): Serializable
}