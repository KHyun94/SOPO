package com.delivery.sopo.models.parcel

import com.google.gson.annotations.SerializedName

data class UpdatableParcel(
        @SerializedName("parcel")
        val parcelResponse: ParcelResponse,
        @SerializedName("updated")
        val updated: Boolean
)
