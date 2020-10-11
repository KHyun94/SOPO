package com.delivery.sopo.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// 
data class LoginResult(
    @SerializedName("userName")
    val userName: String,
    @SerializedName("status")
    val status: Int,
    @SerializedName("regDt")
    val regDt: String,
    @SerializedName("password")
    val password: String
) : Serializable