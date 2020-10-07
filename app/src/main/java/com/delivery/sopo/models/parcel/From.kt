package com.delivery.sopo.models.parcel

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class From(
    @SerializedName("name")
    val name: String?,
    @SerializedName("time")
    val time: String?
): Serializable
