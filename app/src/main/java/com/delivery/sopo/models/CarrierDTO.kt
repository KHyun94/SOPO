package com.delivery.sopo.models

import com.delivery.sopo.enums.CarrierEnum
import com.google.gson.annotations.SerializedName
import java.io.Serializable

// Room - Courier 테이블의 값을 받아오는 모델
data class CarrierDTO(
    @SerializedName("carrier")
    val carrier: CarrierEnum,
    @SerializedName("clickRes")
    val clickRes: Int,
    @SerializedName("nonClickRes")
    val nonClickRes: Int,
    @SerializedName("iconRes")
    val iconRes: Int
) : Serializable