package com.delivery.sopo.models.parcel.tracking_info

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Status (
    @SerializedName("id")
    val id: String?,
    @SerializedName("text")
    val text: String?
): Serializable