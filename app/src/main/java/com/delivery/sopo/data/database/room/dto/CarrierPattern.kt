package com.delivery.sopo.data.database.room.dto

import com.delivery.sopo.enums.CarrierEnum
import com.google.gson.annotations.SerializedName

data class CarrierPattern(
        @SerializedName("carrier")
        val code: String,
        @SerializedName("length")
        val length: Int,
        @SerializedName("header")
        val header: String,
        @SerializedName("priority")
        val priority: Float
){
        val carrierEnum: CarrierEnum by lazy {
                CarrierEnum.getCarrierByCode(code)
        }
}
