package com.delivery.sopo.networks.dto.joins

import com.google.gson.annotations.SerializedName

data class JoinInfo(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("kakaoUid")
    val kakaoUid: String? = null,
    @SerializedName("nickname")
    val nickname: String? = null
)
