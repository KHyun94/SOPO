package com.delivery.sopo.models

import com.delivery.sopo.enums.CarrierEnum
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ParcelRegister(
        @SerializedName("waybillNum")
        var waybillNum: String?,
        @SerializedName("carrier")
        var carrier: CarrierEnum?,
        @SerializedName("alias")
        var alias: String?
):Serializable