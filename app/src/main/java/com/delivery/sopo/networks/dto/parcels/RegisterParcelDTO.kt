package com.delivery.sopo.networks.dto.parcels

import com.google.gson.annotations.SerializedName
import retrofit2.http.Field

data class RegisterParcelDTO(
    @SerializedName("trackCompany")
    val trackCompany: String,
    @SerializedName("trackNum")
    val trackNum: String,
    @SerializedName("parcelAlias")
    val parcelAlias: String? = null
)