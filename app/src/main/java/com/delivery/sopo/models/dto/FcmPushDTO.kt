package com.delivery.sopo.models.dto

data class FcmPushDTO(
    val notificationId: String,
    val regDt: String,
    val parcelUid: String,
    val deliveryStatus: String
)