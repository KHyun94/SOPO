package com.delivery.sopo.models.parcel

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class To(
    @SerializedName("name")
    val name: String?,
    @SerializedName("time")
    val time: String?
): Serializable