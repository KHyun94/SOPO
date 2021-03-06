package com.delivery.sopo.models.parcel

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class ParcelId(
    @SerializedName("regDt")
    val regDt: String,
    @SerializedName("parcelUid")
    var parcelUid: String,
    @SerializedName("inquiryHash")
    var inquiryHash : String? = null
)