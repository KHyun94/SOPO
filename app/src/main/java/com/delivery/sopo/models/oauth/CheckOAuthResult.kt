package com.delivery.sopo.models.oauth

import com.google.gson.annotations.SerializedName

data class CheckOAuthResult(
    @SerializedName("aud")
    val aud : List<String>,
    @SerializedName("user_id")
    val userId : String,
    @SerializedName("scope")
    val scope : String,
    @SerializedName("active")
    val active : Boolean,
    @SerializedName("exp")
    val exp : Int,
    @SerializedName("authorities")
    val authorities : List<String>,
    @SerializedName("client_id")
    val clientId : String
)
