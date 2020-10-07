package com.delivery.sopo.models.parcel

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Carrier(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("tel")
    val tel: String?
): Serializable