package com.delivery.sopo.models.parcel

import com.google.gson.annotations.SerializedName

data class TrackingInfo(
    @SerializedName("from")
    val from: From?,
    @SerializedName("to")
    val to: To?,
    @SerializedName("state")
    val state: State,
    @SerializedName("item")
    val item: String?,
    @SerializedName("progresses")
    val progresses: MutableList<Progresses?>,
    @SerializedName("carrier")
    val carrier: Carrier?
)