package com.delivery.sopo.data.models

import com.delivery.sopo.models.PersonalMessage
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class User
{
    data class Info(
            @SerializedName("userId") val userName: String,
            @SerializedName("joinType") val joinType: String,
            @SerializedName("nickname") var nickname: String?,
            @SerializedName("personalMessage") val personalMessage: PersonalMessage):Serializable
}