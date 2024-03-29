package com.delivery.sopo.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// 
data class LoginResult(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("nickName")
    val userNickname : String?,
    @SerializedName("status")
    val status: Int,
    @SerializedName("regDt")
    val regDt: String,
    @SerializedName("password")
    val password: String
) : Serializable