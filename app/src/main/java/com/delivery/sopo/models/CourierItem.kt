package com.delivery.sopo.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// Room - Courier 테이블의 값을 받아오는 모델
data class CourierItem(
    @SerializedName("courierName")
    val courierName: String,
    @SerializedName("courierCode")
    val courierCode: String,
    @SerializedName("clickRes")
    val clickRes: Int,
    @SerializedName("nonClickRes")
    val nonClickRes: Int,
    @SerializedName("iconRes")
    val iconRes: Int
) : Serializable