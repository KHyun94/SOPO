package com.delivery.sopo.models.parcel

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class Progresses (
    @SerializedName("time")
    val time: String?,
    @SerializedName("location")
    val location: Location?,
    @SerializedName("status")
    val status:  Status?,
    @SerializedName("description")
    val description: String?
) : Serializable