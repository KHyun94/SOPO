package com.delivery.sopo.models

import com.google.gson.annotations.SerializedName

data class UserDetail(
        @SerializedName("userId") val userName: String,
        @SerializedName("joinType") val joinType: String,
        @SerializedName("nickname") var nickname: String?,
        @SerializedName("personalMessage") val personalMessage: PersonalMessage)
