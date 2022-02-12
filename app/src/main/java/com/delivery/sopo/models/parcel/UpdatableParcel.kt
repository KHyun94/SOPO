package com.delivery.sopo.models.parcel

import com.google.gson.annotations.SerializedName

data class UpdatableParcel(
        @SerializedName("parcel")
        val parcelResponse: Parcel.Common,
        @SerializedName("updated")
        val updated: Boolean
)
