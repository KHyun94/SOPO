package com.delivery.sopo.models.parcel

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class Parcel(
    @SerializedName("parcelId")
    val parcelId: ParcelId,
    @SerializedName("userName")
    val userName: String,
    @SerializedName("trackNum")
    val trackNum: String,
    @SerializedName("carrier")
    val carrier: String,
    @SerializedName("parcelAlias")
    val parcelAlias: String,
    @SerializedName("inqueryResult")
    val inqueryResult: String,
    @SerializedName("inqueryHash")
    val inqueryHash: String,
    @SerializedName("deliveryStatus")
    val deliveryStatus: String,
    @SerializedName("arrivalDte")
    val arrivalDte: String?,
    @SerializedName("auditDte")
    val auditDte: String,
    @SerializedName("status")
    val status: Int?
)