package com.delivery.sopo.networks.dto.parcels

import com.delivery.sopo.enums.CarrierEnum
import com.google.gson.annotations.SerializedName
import retrofit2.http.Field

data class RegisterParcelDTO(
    @SerializedName("carrier")
    val carrier: CarrierEnum,
    @SerializedName("waybillNum")
    val waybillNum: String,
    @SerializedName("alias")
    val alias: String? = null
)