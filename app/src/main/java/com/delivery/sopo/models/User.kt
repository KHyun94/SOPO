package com.delivery.sopo.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val pwd: String,
    @SerializedName("deviceInfo")
    val deviceInfo: String,
    @SerializedName("joinType")
    val joinType: String
    //자체:self, 카카오:kakao
)
