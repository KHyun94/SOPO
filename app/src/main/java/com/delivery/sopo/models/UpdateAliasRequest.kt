package com.delivery.sopo.models

import com.google.gson.annotations.SerializedName

data class UpdateAliasRequest(
        @SerializedName("parcelId") val parcelId: Int,
        @SerializedName("alias") val alias: String)
