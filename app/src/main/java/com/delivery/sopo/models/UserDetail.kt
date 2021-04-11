package com.delivery.sopo.models

import com.google.gson.annotations.SerializedName

data class UserDetail(
    @SerializedName("userName")
    val userName : String,
    @SerializedName("joinType")
    val joinType : String,
    @SerializedName("nickName")
    var nickname : String
)