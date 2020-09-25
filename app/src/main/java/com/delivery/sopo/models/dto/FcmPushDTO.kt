package com.delivery.sopo.models.dto

data class FcmPushDTO(
    val update: Boolean,
    val notificationId: String,
    val regDt: String,
    val parcelUid: String
)