package com.delivery.sopo.models.parcel.tracking_info

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Location(
    @SerializedName("name")
    val name: String
) : Serializable