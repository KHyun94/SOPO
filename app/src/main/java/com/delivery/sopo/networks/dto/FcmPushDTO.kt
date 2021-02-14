package com.delivery.sopo.networks.dto

import com.google.gson.annotations.SerializedName

data class FcmPushDTO(
    @SerializedName("notificationId")
    val notificationId: String,
    @SerializedName("regDt")
    val regDt: String,
    @SerializedName("parcelUid")
    val parcelUid: String,
    @SerializedName("deliveryStatus")
    val deliveryStatus: String
)