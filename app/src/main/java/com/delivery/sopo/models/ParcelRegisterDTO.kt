package com.delivery.sopo.models

import com.delivery.sopo.enums.CarrierEnum
import java.io.Serializable

data class ParcelRegisterDTO(
        var waybillNum: String?,
        var carrier: CarrierEnum?,
        var alias: String?
):Serializable