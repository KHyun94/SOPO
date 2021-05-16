package com.delivery.sopo.models

import com.google.gson.annotations.SerializedName

data class UserDetail(
    @SerializedName("userId")
    val userId : String,
    @SerializedName("joinType")
    val joinType : String,
    @SerializedName("nickname")
    var nickname : String?
)