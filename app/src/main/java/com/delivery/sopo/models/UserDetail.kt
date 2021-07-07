package com.delivery.sopo.models

import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.util.SopoLog
import com.google.gson.annotations.SerializedName
import org.koin.core.KoinComponent
import org.koin.core.inject

data class UserDetail(
    @SerializedName("userId")
    val userId : String,
    @SerializedName("joinType")
    val joinType : String,
    @SerializedName("nickname")
    var nickname : String?,
    @SerializedName("personalMessage")
    val personalMessage: PersonalMessage
)
